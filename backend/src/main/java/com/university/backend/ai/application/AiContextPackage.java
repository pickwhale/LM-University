package com.university.backend.ai.application;

import com.university.backend.ai.dto.AiSourceResponse;
import java.util.List;

public record AiContextPackage(String contextText, List<AiSourceResponse> sources) {
}
