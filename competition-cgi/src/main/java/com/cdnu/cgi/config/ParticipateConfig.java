package com.cdnu.cgi.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipateConfig {
    @JsonProperty("team_max_participants")
    private Long teamMaxParticipants;
    @JsonProperty("member_max_participants")
    private Long memberMaxParticipants;
    @JsonProperty("individual_max_participants")
    private Long individualMaxParticipants;
}
