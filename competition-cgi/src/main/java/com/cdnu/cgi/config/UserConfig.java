package com.cdnu.cgi.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserConfig {
    @JsonProperty("avatar_path")
    private String avatarPath;
    @JsonProperty("honor_path")
    private String honorPath;
    @JsonProperty("project_path")
    private String projectPath;
}
