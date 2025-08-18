package com.example.music_service.dto.spotify;

import lombok.Data;
import java.util.List;

@Data
public class PlaylistDto {
    private String id;
    private String name;
    private String description;
    private boolean collaborative;
    private String snapshot_id;
    private String uri;
    private String href;

    private ExternalUrls external_urls;
    private OwnerDto owner;
    private Followers followers;
    private TracksWrapper tracks;
    private List<Image> images;

    @Data
    public static class TracksWrapper {
        private String href;
        private int total;
        private int limit;
        private int offset;
        private String next;
        private String previous;
        private List<TrackItem> items;
    }

    @Data
    public static class TrackItem {
        private String added_at;
        private boolean is_local;
        private TrackDto track;
    }


    @Data
    public static class Followers {
        private int total;
    }

    @Data
    public static class Image {
        private String url;
        private int height;
        private int width;
    }

    @Data
    public static class ExternalUrls {
        private String spotify;
    }
}
