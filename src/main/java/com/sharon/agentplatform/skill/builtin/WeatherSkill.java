package com.sharon.agentplatform.skill.builtin;

import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillContext;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class WeatherSkill implements Skill {

    @Override
    public SkillMetadata metadata() {
        return new SkillMetadata(
                "weather",
                "天气查询",
                "根据城市名称查询天气信息。当前版本使用模拟数据。",
                "1.0.0",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "city", Map.of(
                                        "type", "string",
                                        "description", "城市名称，例如 北京、上海、广州"
                                )
                        ),
                        "required", List.of("city")
                ),
                List.of()
        );
    }

    @Override
    public SkillResult execute(SkillContext context) {
        String city = context.getStringParam("city");

        if (city == null || city.isBlank()) {
            return SkillResult.fail("Missing required parameter: city");
        }

        Map<String, Object> result = mockWeather(city);

        return SkillResult.success(result);
    }

    private Map<String, Object> mockWeather(String city) {
        return switch (city) {
            case "北京" -> Map.of(
                    "city", city,
                    "weather", "晴",
                    "temperature", "22°C",
                    "humidity", "35%",
                    "source", "mock"
            );
            case "上海" -> Map.of(
                    "city", city,
                    "weather", "多云",
                    "temperature", "25°C",
                    "humidity", "60%",
                    "source", "mock"
            );
            case "广州" -> Map.of(
                    "city", city,
                    "weather", "小雨",
                    "temperature", "28°C",
                    "humidity", "75%",
                    "source", "mock"
            );
            default -> Map.of(
                    "city", city,
                    "weather", "未知",
                    "temperature", "未知",
                    "humidity", "未知",
                    "source", "mock"
            );
        };
    }
}