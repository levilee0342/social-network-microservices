package com.example.user_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.LookupInResult;
import com.couchbase.client.java.kv.LookupInSpec;
import com.couchbase.client.java.kv.MutateInSpec;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.example.user_service.dto.request.profile_details.*;
import com.example.user_service.dto.response.PublicProfileDetailsResponse;
import com.example.user_service.dto.response.profile_details.*;
import com.example.user_service.entity.PublicField;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.error.InvalidErrorCode;
import com.example.user_service.error.NotExistedErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseProfileDetailRepository;
import com.example.user_service.service.interfaces.IProfileDetailsMapperService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class CouchbaseProfileDetailRepository implements ICouchbaseProfileDetailRepository {
    private final Collection profileDetailCollection;
    private final IProfileDetailsMapperService IProfileDetailsMapperService;
    private final Cluster cluster;
    private final ProducerLog logProducer;

    public CouchbaseProfileDetailRepository(@Qualifier("profileCouchbaseCluster") Cluster cluster,
                                            @Qualifier("profileBucket") Bucket profileBucket,
                                            IProfileDetailsMapperService IProfileDetailsMapperService,
                                            ProducerLog logProducer) {
        this.cluster = cluster;
        this.profileDetailCollection = profileBucket.scope("user_information").collection("profile_details");
        this.IProfileDetailsMapperService = IProfileDetailsMapperService;
        this.logProducer = logProducer;
    }

    @Override
    public PublicProfileDetailsResponse findByUserId(String userId) {
        List<JsonObject> rows = new ArrayList<>();
        try {
            String query = "SELECT pd.* FROM `profile_bucket`.`user_information`.`profile_details` pd WHERE pd.userId = $userId LIMIT 1";
            QueryResult result = cluster.query(query, QueryOptions.queryOptions().parameters(JsonObject.create().put("userId", userId)));
             rows = result.rowsAsObject();
            if (rows.isEmpty()) {
                logProducer.sendLog(
                        "user-service",
                        "DEBUG",
                        "[CouchbaseProfileDetailRepository] No profile found for userId: " + userId
                );
                return null;
            }
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[CouchbaseProfileDetailRepository] Retrieved JSON for userId: " + userId + ", JSON: " + rows.get(0).toString()
            );
            return IProfileDetailsMapperService.mapJsonToProfileDetails(rows.get(0));
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[CouchbaseProfileDetailRepository] findByUserId failed for userId: " + userId +
                            ", JSON: " + (rows.isEmpty() ? "empty" : rows.get(0).toString()) +
                            ", Reason: " + e.getClass().getName() + ": " + e.getMessage()
            );
            throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE_DETAIL);
        }
    }

    @Override
    public PublicProfileDetailsResponse createDefaultProfileDetails(String userId) {
        try {
            String id = UUID.randomUUID().toString();
            JsonObject doc = JsonObject.create()
                    .put("profileDetailsId", id)
                    .put("userId", userId)
                    .put("createdAt", new Date().getTime())
                    .put("updatedAt", new Date().getTime());
            profileDetailCollection.insert(id, doc);
            return IProfileDetailsMapperService.mapJsonToProfileDetails(doc);
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[CouchbaseProfileDetailRepository] createDefaultProfileDetails failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_CREATE_PROFILE_DETAIL);
        }
    }

    @Override
    public EducationResponse addEducation(String userId, EducationRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) {
                profile = createDefaultProfileDetails(userId);
            }
            if (profile.getProfileDetailsId() == null) {
                throw new RuntimeException("profileDetailsId is null");
            }

            EducationResponse edu = new EducationResponse();
            edu.setSchoolName(request.getSchoolName());
            edu.setMajor(request.getMajor());
            edu.setStartYear(request.getStartYear());
            edu.setEndYear(request.getEndYear());
            edu.setGraduate(request.getGraduate());
            edu.setDescription(request.getDescription());
            edu.setIsPublic(request.getIsPublic());

            JsonObject eduJson = JsonObject.create()
                    .put("schoolName", edu.getSchoolName())
                    .put("major", edu.getMajor())
                    .put("startYear", edu.getStartYear())
                    .put("endYear", edu.getEndYear())
                    .put("graduate", edu.getGraduate())
                    .put("description", edu.getDescription())
                    .put("isPublic", edu.getIsPublic());

            profileDetailCollection.mutateIn(profile.getProfileDetailsId(), Collections.singletonList(
                    MutateInSpec.arrayAppend("education", Collections.singletonList(eduJson)).createPath()
            ));

            return edu;
        } catch (Exception e) {
            e.printStackTrace();
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[addEducation] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    @Override
    public EducationResponse updateEducationByIndex(String userId, int index, EducationRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) {
                throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);
            }


            String docId = profile.getProfileDetailsId();

            LookupInResult result = profileDetailCollection.lookupIn(docId,
                    Collections.singletonList(LookupInSpec.count("education")));
            int total = result.contentAs(0, Integer.class);
            if (index < 0 || index >= total)
                throw new AppException(InvalidErrorCode.INVALID_INDEX);

            String basePath = "education[" + index + "]";

            List<MutateInSpec> specs = new ArrayList<>();
            specs.add(MutateInSpec.upsert(basePath + ".schoolName", request.getSchoolName()));
            specs.add(MutateInSpec.upsert(basePath + ".major", request.getMajor()));
            specs.add(MutateInSpec.upsert(basePath + ".startYear", request.getStartYear()));
            specs.add(MutateInSpec.upsert(basePath + ".endYear", request.getEndYear()));
            specs.add(MutateInSpec.upsert(basePath + ".graduate", request.getGraduate()));
            specs.add(MutateInSpec.upsert(basePath + ".description", request.getDescription()));
            specs.add(MutateInSpec.upsert(basePath + ".isPublic", request.getIsPublic()));

            profileDetailCollection.mutateIn(docId, specs);
            EducationResponse response = new EducationResponse();
            BeanUtils.copyProperties(request, response);
            return response;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[updateEducationByIndex] Failed: " + e.getMessage());
            e.printStackTrace();
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    @Override
    public boolean deleteEducationByIndex(String userId, int index) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();

            LookupInResult result = profileDetailCollection.lookupIn(docId,
                    Collections.singletonList(LookupInSpec.count("education")));
            int total = result.contentAs(0, Integer.class);
            if (index < 0 || index >= total)
                throw new AppException(InvalidErrorCode.INVALID_INDEX);

            String path = "education[" + index + "]";
            profileDetailCollection.mutateIn(docId, Collections.singletonList(
                    MutateInSpec.remove(path)
            ));
            return true;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[deleteEducationByIndex] Failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public WorkResponse addWork(String userId, WorkRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) {
                profile = createDefaultProfileDetails(userId);
            }
            if (profile.getProfileDetailsId() == null) {
                throw new RuntimeException("profileDetailsId is null");
            }

            WorkResponse workResponse = new WorkResponse();
            workResponse.setCompany(request.getCompany());
            workResponse.setPosition(request.getPosition());
            workResponse.setStartDate(request.getStartDate());
            workResponse.setEndDate(request.getEndDate());
            workResponse.setDescription(request.getDescription());
            workResponse.setIsPublic(request.getIsPublic());

            JsonObject workJson = JsonObject.create()
                    .put("company", workResponse.getCompany())
                    .put("position", workResponse.getPosition())
                    .put("startDate", workResponse.getStartDate())
                    .put("endDate", workResponse.getEndDate())
                    .put("description", workResponse.getDescription())
                    .put("isPublic", workResponse.getIsPublic());

            profileDetailCollection.mutateIn(profile.getProfileDetailsId(), Collections.singletonList(
                    MutateInSpec.arrayAppend("work", Collections.singletonList(workJson)).createPath()
            ));

            return workResponse;
        } catch (Exception e) {
            e.printStackTrace();
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[addWork] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    @Override
    public WorkResponse updateWorkByIndex(String userId, int index, WorkRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();

            LookupInResult result = profileDetailCollection.lookupIn(docId,
                    Collections.singletonList(LookupInSpec.count("work")));
            int total = result.contentAs(0, Integer.class);
            if (index < 0 || index >= total)
                throw new AppException(InvalidErrorCode.INVALID_INDEX);

            String basePath = "work[" + index + "]";

            List<MutateInSpec> specs = new ArrayList<>();
            specs.add(MutateInSpec.upsert(basePath + ".company", request.getCompany()));
            specs.add(MutateInSpec.upsert(basePath + ".position", request.getPosition()));
            specs.add(MutateInSpec.upsert(basePath + ".startDate", request.getStartDate()));
            specs.add(MutateInSpec.upsert(basePath + ".endDate", request.getEndDate()));
            specs.add(MutateInSpec.upsert(basePath + ".description", request.getDescription()));
            specs.add(MutateInSpec.upsert(basePath + ".isPublic", request.getIsPublic()));

            profileDetailCollection.mutateIn(docId, specs);
            WorkResponse response = new WorkResponse();
            BeanUtils.copyProperties(request, response);
            return response;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[updateWorkByIndex] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    @Override
    public boolean deleteWorkByIndex(String userId, int index) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();

            LookupInResult result = profileDetailCollection.lookupIn(docId,
                    Collections.singletonList(LookupInSpec.count("work")));
            int total = result.contentAs(0, Integer.class);
            if (index < 0 || index >= total)
                throw new AppException(InvalidErrorCode.INVALID_INDEX);

            String path = "work[" + index + "]";
            profileDetailCollection.mutateIn(docId, Collections.singletonList(
                    MutateInSpec.remove(path)
            ));
            return true;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[deleteWorkByIndex] Failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public FamilyMemberResponse addFamilyMember(String userId, FamilyMemberRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) {
                profile = createDefaultProfileDetails(userId);
            }
            if (profile.getProfileDetailsId() == null) {
                throw new RuntimeException("profileDetailsId is null");
            }

            FamilyMemberResponse familyMemberResponse = new FamilyMemberResponse();
            familyMemberResponse.setUserId(request.getUserId());
            familyMemberResponse.setRelationship(request.getRelationship());
            familyMemberResponse.setIsPublic(request.getIsPublic());

            JsonObject familyMemberJson = JsonObject.create()
                    .put("userId", familyMemberResponse.getUserId())
                    .put("relationship", familyMemberResponse.getRelationship())
                    .put("isPublic", familyMemberResponse.getIsPublic());

            profileDetailCollection.mutateIn(profile.getProfileDetailsId(), Collections.singletonList(
                    MutateInSpec.arrayAppend("familyMember", Collections.singletonList(familyMemberJson)).createPath()
            ));

            return familyMemberResponse;
        } catch (Exception e) {
            e.printStackTrace();
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[addFamilyMember] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    @Override
    public FamilyMemberResponse updateFamilyMemberByIndex(String userId, int index, FamilyMemberRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();

            LookupInResult result = profileDetailCollection.lookupIn(docId,
                    Collections.singletonList(LookupInSpec.count("familyMember")));
            int total = result.contentAs(0, Integer.class);
            if (index < 0 || index >= total)
                throw new AppException(InvalidErrorCode.INVALID_INDEX);

            String basePath = "familyMember[" + index + "]";

            List<MutateInSpec> specs = new ArrayList<>();
            specs.add(MutateInSpec.upsert(basePath + ".userId", request.getUserId()));
            specs.add(MutateInSpec.upsert(basePath + ".relationship", request.getRelationship()));
            specs.add(MutateInSpec.upsert(basePath + ".isPublic", request.getIsPublic()));

            profileDetailCollection.mutateIn(docId, specs);
            FamilyMemberResponse response = new FamilyMemberResponse();
            BeanUtils.copyProperties(request, response);
            return response;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[updateFamilyMemberByIndex] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    @Override
    public boolean deleteFamilyMemberByIndex(String userId, int index) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();

            LookupInResult result = profileDetailCollection.lookupIn(docId,
                    Collections.singletonList(LookupInSpec.count("familyMember")));
            int total = result.contentAs(0, Integer.class);
            if (index < 0 || index >= total)
                throw new AppException(InvalidErrorCode.INVALID_INDEX);

            String path = "familyMember[" + index + "]";
            profileDetailCollection.mutateIn(docId, Collections.singletonList(
                    MutateInSpec.remove(path)
            ));
            return true;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[deleteWorkByIndex] Failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public RelationshipResponse updateRelationship(String userId, RelationshipRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();
            String basePath = "relationship";

            List<MutateInSpec> specs = new ArrayList<>();
            specs.add(MutateInSpec.upsert(basePath + ".status", request.getStatus()));
            specs.add(MutateInSpec.upsert(basePath + ".partnerName", request.getPartnerName()));
            specs.add(MutateInSpec.upsert(basePath + ".startDate", request.getStartDate()));
            specs.add(MutateInSpec.upsert(basePath + ".isPublic", request.getIsPublic()));

            profileDetailCollection.mutateIn(docId, specs);

            RelationshipResponse response = new RelationshipResponse();
            BeanUtils.copyProperties(request, response);
            return response;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[updateRelationship] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    @Override
    public boolean deleteRelationship(String userId) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();

            String path = "relationship";
            profileDetailCollection.mutateIn(docId, Collections.singletonList(
                    MutateInSpec.remove(path)
            ));
            return true;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[deleteRelationship] Failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public LocationResponse updateLocation(String userId, LocationRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();
            String basePath = "location";

            List<MutateInSpec> specs = new ArrayList<>();

            if (request.getCurrentCity() != null) {
                specs.add(MutateInSpec.upsert(basePath + ".currentCity.value", request.getCurrentCity().getValue()).createPath());
                specs.add(MutateInSpec.upsert(basePath + ".currentCity.isPublic", request.getCurrentCity().getIsPublic()).createPath());
            }

            if (request.getHometown() != null) {
                specs.add(MutateInSpec.upsert(basePath + ".hometown.value", request.getHometown().getValue()).createPath());
                specs.add(MutateInSpec.upsert(basePath + ".hometown.isPublic", request.getHometown().getIsPublic()).createPath());
            }

            profileDetailCollection.mutateIn(docId, specs);

            LocationResponse response = new LocationResponse();
            BeanUtils.copyProperties(request, response);
            return response;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[updateLocation] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    public boolean deleteLocationField(String userId, String fieldToDelete) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();
            String path = "location." + fieldToDelete;

            profileDetailCollection.mutateIn(docId, Collections.singletonList(
                    MutateInSpec.remove(path)
            ));

            return true;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[deleteLocationField] Failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public ContactResponse updateContact(String userId, ContactRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();
            String basePath = "contact";

            List<MutateInSpec> specs = new ArrayList<>();

            if (request.getPhone() != null) {
                specs.add(MutateInSpec.upsert(basePath + ".phone.value", request.getPhone().getValue()).createPath());
                specs.add(MutateInSpec.upsert(basePath + ".phone.isPublic", request.getPhone().getIsPublic()).createPath());
            }

            if (request.getEmail() != null) {
                specs.add(MutateInSpec.upsert(basePath + ".email.value", request.getEmail().getValue()).createPath());
                specs.add(MutateInSpec.upsert(basePath + ".email.isPublic", request.getEmail().getIsPublic()).createPath());
            }

            if (request.getWebsite() != null) {
                specs.add(MutateInSpec.upsert(basePath + ".website.value", request.getWebsite().getValue()).createPath());
                specs.add(MutateInSpec.upsert(basePath + ".website.isPublic", request.getWebsite().getIsPublic()).createPath());
            }

            if (request.getSocialLinks() != null) {
                for (Map.Entry<String, PublicField<String>> entry : request.getSocialLinks().entrySet()) {
                    String key = entry.getKey();
                    PublicField<String> value = entry.getValue();
                    if (value != null) {
                        specs.add(MutateInSpec.upsert(basePath + ".socialLinks." + key + ".value", value.getValue()).createPath());
                        specs.add(MutateInSpec.upsert(basePath + ".socialLinks." + key + ".isPublic", value.getIsPublic()).createPath());
                    }
                }
            }

            profileDetailCollection.mutateIn(docId, specs);

            ContactResponse response = new ContactResponse();
            BeanUtils.copyProperties(request, response);
            return response;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[updateContact] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    public boolean deleteContactField(String userId, String fieldName, String socialKey) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null)
                throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();
            String path;

            if (fieldName != null && List.of("phone", "email", "website").contains(fieldName)) {
                path = "contact." + fieldName;
            } else if ("socialLinks".equals(fieldName) && socialKey != null) {
                path = "contact.socialLinks." + socialKey;
            } else {
                throw new AppException(InvalidErrorCode.INVALID_PARAM);
            }

            profileDetailCollection.mutateIn(docId, Collections.singletonList(
                    MutateInSpec.remove(path)
            ));

            return true;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[deleteContactField] Failed: " + e.getMessage());
            return false;
        }
    }


    @Override
    public DetailResponse updateDetail(String userId, DetailRequest request) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null)
                throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();
            String basePath = "detail";

            List<MutateInSpec> specs = new ArrayList<>();

            if (request.getQuote() != null) {
                specs.add(MutateInSpec.upsert(basePath + ".quote.value", request.getQuote().getValue()).createPath());
                specs.add(MutateInSpec.upsert(basePath + ".quote.isPublic", request.getQuote().getIsPublic()).createPath());
            }

            if (request.getNickname() != null) {
                specs.add(MutateInSpec.upsert(basePath + ".nickname.value", request.getNickname().getValue()).createPath());
                specs.add(MutateInSpec.upsert(basePath + ".nickname.isPublic", request.getNickname().getIsPublic()).createPath());
            }

            if (request.getBloodType() != null) {
                specs.add(MutateInSpec.upsert(basePath + ".bloodType.value", request.getBloodType().getValue()).createPath());
                specs.add(MutateInSpec.upsert(basePath + ".bloodType.isPublic", request.getBloodType().getIsPublic()).createPath());
            }

            if (request.getReligion() != null) {
                specs.add(MutateInSpec.upsert(basePath + ".religion.value", request.getReligion().getValue()).createPath());
                specs.add(MutateInSpec.upsert(basePath + ".religion.isPublic", request.getReligion().getIsPublic()).createPath());
            }

            if (request.getLanguages() != null) {
                for (int i = 0; i < request.getLanguages().size(); i++) {
                    PublicField<String> lang = request.getLanguages().get(i);
                    specs.add(MutateInSpec.upsert(basePath + ".languages[" + i + "].value", lang.getValue()).createPath());
                    specs.add(MutateInSpec.upsert(basePath + ".languages[" + i + "].isPublic", lang.getIsPublic()).createPath());
                }
            }

            profileDetailCollection.mutateIn(docId, specs);

            DetailResponse response = new DetailResponse();
            BeanUtils.copyProperties(request, response);
            return response;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[updateDetail] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE_DETAIL);
        }
    }

    @Override
    public boolean deleteDetailField(String userId, String fieldToDelete) {
        try {
            PublicProfileDetailsResponse profile = findByUserId(userId);
            if (profile == null)
                throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);

            String docId = profile.getProfileDetailsId();
            String path = "detail." + fieldToDelete;

            profileDetailCollection.mutateIn(docId, Collections.singletonList(
                    MutateInSpec.remove(path)
            ));
            return true;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[deleteDetailField] Failed: " + e.getMessage());
            return false;
        }
    }


}
