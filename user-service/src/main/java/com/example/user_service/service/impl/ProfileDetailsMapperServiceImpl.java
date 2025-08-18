package com.example.user_service.service.impl;

import com.couchbase.client.java.json.JsonObject;
import com.example.user_service.dto.response.PublicProfileDetailsResponse;
import com.example.user_service.dto.response.profile_details.*;
import com.example.user_service.entity.PublicField;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.service.interfaces.IProfileDetailsMapperService;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileDetailsMapperServiceImpl implements IProfileDetailsMapperService {
    private final ProducerLog logProducer;

    public ProfileDetailsMapperServiceImpl(ProducerLog logProducer) {
        this.logProducer = logProducer;
    }

    @Override
    public PublicProfileDetailsResponse mapToResponseForOwner(PublicProfileDetailsResponse profile) {
        try {
            PublicProfileDetailsResponse response = new PublicProfileDetailsResponse();
            response.setProfileDetailsId(profile.getProfileDetailsId());
            response.setUserId(profile.getUserId());
            response.setEducation(
                    Optional.ofNullable(profile.getEducation()).orElse(Collections.emptyList())
            );
            response.setWork(
                    Optional.ofNullable(profile.getWork()).orElse(Collections.emptyList())
            );
            response.setFamilyMembers(
                    Optional.ofNullable(profile.getFamilyMembers()).orElse(Collections.emptyList())
            );
            response.setLocation(profile.getLocation());
            response.setContact(profile.getContact());
            response.setRelationship(profile.getRelationship());
            response.setDetail(profile.getDetail());
            response.setCreatedAt(profile.getCreatedAt());
            response.setUpdatedAt(profile.getUpdatedAt());

            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[ProfileDetailMapper] Successfully mapped full ProfileDetailsResponse for owner userId: " + profile.getUserId()
            );
            return response;

        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[ProfileDetailMapper] Failed to map full ProfileDetailsResponse for owner userId: " + profile.getUserId()
                            + ". Reason: " + e.getMessage()
            );
            throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE_DETAIL);
        }
    }

    @Override
    public PublicProfileDetailsResponse mapToResponse(PublicProfileDetailsResponse profile) {
        try {
            PublicProfileDetailsResponse response = new PublicProfileDetailsResponse();
            response.setProfileDetailsId(profile.getProfileDetailsId());
            response.setUserId(profile.getUserId());
            response.setEducation(
                    Optional.ofNullable(profile.getEducation())
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(EducationResponse::getIsPublic)
                            .collect(Collectors.toList())
            );
            response.setWork(
                    Optional.ofNullable(profile.getWork())
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(WorkResponse::getIsPublic)
                            .collect(Collectors.toList())
            );
            LocationResponse location = profile.getLocation();
            if (location != null) {
                response.setLocation(new LocationResponse(
                        filterPublicField(location.getCurrentCity()),
                        filterPublicField(location.getHometown())
                ));
            }
            ContactResponse contact = profile.getContact();
            if (contact != null) {
                ContactResponse filteredContact = new ContactResponse();
                filteredContact.setPhone(filterPublicField(contact.getPhone()));
                filteredContact.setEmail(filterPublicField(contact.getEmail()));
                filteredContact.setWebsite(filterPublicField(contact.getWebsite()));

                if (contact.getSocialLinks() != null) {
                    Map<String, PublicField<String>> publicLinks = contact.getSocialLinks().entrySet().stream()
                            .filter(entry -> entry.getValue() != null && Boolean.TRUE.equals(entry.getValue().getIsPublic()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    filteredContact.setSocialLinks(publicLinks);
                }

                response.setContact(filteredContact);
            }
            RelationshipResponse relationship = profile.getRelationship();
            if (relationship != null && Boolean.TRUE.equals(relationship.getIsPublic())) {
                response.setRelationship(relationship);
            } else {
                response.setRelationship(null);
            }
            response.setFamilyMembers(
                    Optional.ofNullable(profile.getFamilyMembers())
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(FamilyMemberResponse::getIsPublic)
                            .collect(Collectors.toList())
            );
            DetailResponse detail = profile.getDetail();
            if (detail != null) {
                DetailResponse filteredDetail = new DetailResponse();
                filteredDetail.setQuote(filterPublicField(detail.getQuote()));
                filteredDetail.setNickname(filterPublicField(detail.getNickname()));
                filteredDetail.setBloodType(filterPublicField(detail.getBloodType()));
                filteredDetail.setReligion(filterPublicField(detail.getReligion()));

                if (detail.getLanguages() != null) {
                    List<PublicField<String>> langs = detail.getLanguages().stream()
                            .filter(f -> f != null && Boolean.TRUE.equals(f.getIsPublic()))
                            .collect(Collectors.toList());
                    filteredDetail.setLanguages(langs);
                }

                response.setDetail(filteredDetail);
            }

            response.setCreatedAt(profile.getCreatedAt());
            response.setUpdatedAt(profile.getUpdatedAt());
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[ProfileDetailMapper] Successfully mapped PublicProfileDetailsResponse for userId: " + profile.getUserId()
            );
            return response;
        }catch (Exception e){
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[ProfileDetailMapper] Failed mapped PublicProfileDetailsResponse for userId: " + profile.getUserId() + ". Reason: " + e.getMessage()
            );
            throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE_DETAIL);
        }
    }

    @Override
    public PublicProfileDetailsResponse mapJsonToProfileDetails(JsonObject row) {
        logProducer.sendLog(
                "user-service",
                "DEBUG",
                "[ProfileDetailMapper] Starting mapping JSON to PublicProfileDetailsResponse for profileDetailsId: " +
                        row.getString("profileDetailsId") + ", JSON: " + row.toString()
        );
        try {
            PublicProfileDetailsResponse response = new PublicProfileDetailsResponse();
            logProducer.sendLog("user-service", "DEBUG", "[ProfileDetailMapper] Mapping profileDetailsId and userId");
            response.setProfileDetailsId(row.getString("profileDetailsId"));
            response.setUserId(row.getString("userId"));

            // Xử lý createdAt và updatedAt
            logProducer.sendLog("user-service", "DEBUG", "[ProfileDetailMapper] Mapping createdAt and updatedAt");
            response.setCreatedAt(formatDate(parseTimestamp(row.get("createdAt"), "createdAt")));
            response.setUpdatedAt(formatDate(parseTimestamp(row.get("updatedAt"), "updatedAt")));

            // Education
            if (row.containsKey("education")) {
                logProducer.sendLog("user-service", "DEBUG", "[ProfileDetailMapper] Mapping education");
                List<EducationResponse> educationList = new ArrayList<>();
                for (JsonObject e : row.getArray("education").toList().stream()
                        .map(o -> JsonObject.from((Map<String, Object>) o)).toList()) {
                    EducationResponse edu = new EducationResponse();
                    edu.setSchoolName(e.getString("schoolName"));
                    edu.setMajor(e.getString("major"));
                    edu.setStartYear(e.getInt("startYear"));
                    edu.setEndYear(e.getInt("endYear"));
                    edu.setGraduate(e.getBoolean("graduate"));
                    edu.setDescription(e.getString("description"));
                    edu.setIsPublic(e.getBoolean("isPublic"));
                    educationList.add(edu);
                }
                response.setEducation(educationList);
            }

            // Work
            if (row.containsKey("work")) {
                logProducer.sendLog("user-service", "DEBUG", "[ProfileDetailMapper] Mapping work");
                List<WorkResponse> workList = new ArrayList<>();
                for (JsonObject w : row.getArray("work").toList().stream()
                        .map(o -> JsonObject.from((Map<String, Object>) o)).toList()) {
                    WorkResponse work = new WorkResponse();
                    work.setCompany(w.getString("company"));
                    work.setPosition(w.getString("position"));
                    work.setStartDate(formatDate(parseTimestamp(w.get("startDate"), "work.startDate")));
                    work.setEndDate(formatDate(parseTimestamp(w.get("endDate"), "work.endDate")));
                    work.setDescription(w.getString("description"));
                    work.setIsPublic(w.getBoolean("isPublic"));
                    workList.add(work);
                }
                response.setWork(workList);
            }

            // Location
            if (row.containsKey("location")) {
                logProducer.sendLog("user-service", "DEBUG", "[ProfileDetailMapper] Mapping location");
                JsonObject loc = row.getObject("location");
                LocationResponse location = new LocationResponse(
                        parsePublicField(loc.getObject("currentCity")),
                        parsePublicField(loc.getObject("hometown"))
                );
                response.setLocation(location);
            }

            // Contact
            if (row.containsKey("contact")) {
                logProducer.sendLog("user-service", "DEBUG", "[ProfileDetailMapper] Mapping contact");
                JsonObject c = row.getObject("contact");
                ContactResponse contact = new ContactResponse();
                contact.setPhone(parsePublicField(c.getObject("phone")));
                contact.setEmail(parsePublicField(c.getObject("email")));
                contact.setWebsite(parsePublicField(c.getObject("website")));
                if (c.containsKey("socialLinks")) {
                    Map<String, PublicField<String>> socialLinks = new HashMap<>();
                    JsonObject links = c.getObject("socialLinks");
                    for (String key : links.getNames()) {
                        socialLinks.put(key, parsePublicField(links.getObject(key)));
                    }
                    contact.setSocialLinks(socialLinks);
                }
                response.setContact(contact);
            }

            // Relationship
            if (row.containsKey("relationship")) {
                logProducer.sendLog("user-service", "DEBUG", "[ProfileDetailMapper] Mapping relationship");
                JsonObject r = row.getObject("relationship");
                RelationshipResponse rel = new RelationshipResponse();
                rel.setStatus(r.getString("status"));
                rel.setPartnerName(r.getString("partnerName"));
                rel.setStartDate(formatDate(parseTimestamp(r.get("startDate"), "relationship.startDate")));
                rel.setIsPublic(r.getBoolean("isPublic"));
                response.setRelationship(rel);
            }

            // Detail
            if (row.containsKey("detail")) {
                logProducer.sendLog("user-service", "DEBUG", "[ProfileDetailMapper] Mapping detail");
                JsonObject d = row.getObject("detail");
                DetailResponse detail = new DetailResponse();
                detail.setQuote(parsePublicField(d.getObject("quote")));
                detail.setNickname(parsePublicField(d.getObject("nickname")));
                detail.setBloodType(parsePublicField(d.getObject("bloodType")));
                detail.setReligion(parsePublicField(d.getObject("religion")));
                if (d.containsKey("languages")) {
                    List<PublicField<String>> langs = new ArrayList<>();
                    for (Object l : d.getArray("languages").toList()) {
                        langs.add(parsePublicField(JsonObject.from((Map<String, Object>) l)));
                    }
                    detail.setLanguages(langs);
                }
                response.setDetail(detail);
            }

            // Family Members
            if (row.containsKey("familyMembers")) {
                logProducer.sendLog("user-service", "DEBUG", "[ProfileDetailMapper] Mapping familyMembers");
                List<FamilyMemberResponse> list = new ArrayList<>();
                for (JsonObject f : row.getArray("familyMembers").toList().stream()
                        .map(o -> JsonObject.from((Map<String, Object>) o)).toList()) {
                    FamilyMemberResponse fam = new FamilyMemberResponse();
                    fam.setUserId(f.getString("userId"));
                    fam.setRelationship(f.getString("relationship"));
                    fam.setIsPublic(f.getBoolean("isPublic"));
                    list.add(fam);
                }
                response.setFamilyMembers(list);
            }

            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[ProfileDetailMapper] Successfully mapped PublicProfileDetailsResponse for profileDetailsId: " +
                            row.getString("profileDetailsId")
            );
            return response;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[ProfileDetailMapper] Failed to map JSON to PublicProfileDetailResponse for profileDetailsId: " +
                            row.getString("profileDetailsId") + ". JSON: " + row.toString() +
                            ", Reason: " + e.getClass().getName() + ": " + e.getMessage()
            );
            throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE_DETAIL);
        }
    }

    private Date parseTimestamp(Object value, String fieldName) {
        if (value == null) return null;
        if (value instanceof Long) {
            return new Date((Long) value);
        } else if (value instanceof String) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse((String) value);
            } catch (ParseException e) {
                throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE_DETAIL);
            }
        }
        throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE_DETAIL);
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private <T> PublicField<T> parsePublicField(JsonObject json) {
        if (json == null) return null;

        PublicField<T> field = new PublicField<>();
        field.setIsPublic(json.getBoolean("isPublic"));
        try {
            field.setValue((T) json.get("value"));
        } catch (ClassCastException ex) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[ProfileDetailMapper] Failed to parse PublicField<T>. Value: " + json.get("value") + ", Reason: " + ex.getMessage());
            throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE_DETAIL);
        }
        return field;
    }

    private <T> PublicField<T> filterPublicField(PublicField<T> field) {
        if (field == null || Boolean.FALSE.equals(field.getIsPublic())) {
            return null;
        }
        return field;
    }

}
