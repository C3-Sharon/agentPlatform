package com.sharon.agentplatform.skill.builtin;

import com.sharon.agentplatform.skill.core.Skill;
import com.sharon.agentplatform.skill.core.SkillContext;
import com.sharon.agentplatform.skill.core.SkillMetadata;
import com.sharon.agentplatform.skill.core.SkillResult;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CalculatorSkill implements Skill {

    @Override
    public SkillMetadata metadata() {
        return new SkillMetadata(
                "calculator",
                "计算器",
                "用于计算简单数学表达式，例如 1 + 2 * 3",
                "1.0.0",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "expression", Map.of(
                                        "type", "string",
                                        "description", "需要计算的数学表达式"
                                )
                        ),
                        "required", List.of("expression")
                ),
                List.of("exp4j")
        );
    }

    @Override
    public SkillResult execute(SkillContext context) {
        String expression = context.getStringParam("expression");

        if (expression == null || expression.isBlank()) {
            return SkillResult.fail("Missing required parameter: expression");
        }

        if (!isSafeExpression(expression)) {
            return SkillResult.fail("Expression contains illegal characters");
        }

        try {
            Expression exp = new ExpressionBuilder(expression).build();
            double result = exp.evaluate();

            return SkillResult.success(Map.of(
                    "expression", expression,
                    "result", result
            ));
        } catch (Exception e) {
            return SkillResult.fail("Invalid expression: " + e.getMessage());
        }
    }

    private boolean isSafeExpression(String expression) {
        return expression.matches("[0-9+\\-*/().\\s]+");
    }
}