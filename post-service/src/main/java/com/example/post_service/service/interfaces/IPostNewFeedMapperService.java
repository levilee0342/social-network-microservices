package com.example.post_service.service.interfaces;

import com.example.post_service.dto.response.PostResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IPostNewFeedMapperService {
    PostResponse mapRowToPostNewFeedResponse(ResultSet rs) throws SQLException;
}
