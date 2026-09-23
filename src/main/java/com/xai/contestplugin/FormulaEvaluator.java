package com.xai.contestplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Evaluates mathematical formulas for dynamic point calculation.
 * Supports variables and basic arithmetic operations.
 */
public class FormulaEvaluator {
    
    /**
     * Evaluate a formula with given variables.
     * 
     * Formula examples:
     * - "base * 2"
     * - "base + (y / 10)"
     * - "(base + streak * 5) * chain"
     * - "base * ({health} / 20)"
     * 
     * @param formula The formula string
     * @param variables Map of variable names to values
     * @return Calculated result
     */
    public int evaluate(String formula, Map<String, String> variables) {
        if (formula == null || formula.isEmpty()) {
            return Integer.parseInt(variables.getOrDefault("base", "1"));
        }
        
        // Replace variables
        String expression = formula;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            expression = expression.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        try {
            // Evaluate the mathematical expression
            double result = evaluateExpression(expression);
            return (int) Math.round(result);
        } catch (Exception e) {
            // If formula evaluation fails, return base points
            return Integer.parseInt(variables.getOrDefault("base", "1"));
        }
    }
    
    /**
     * Build default variables for formula evaluation.
     */
    public Map<String, String> buildVariables(Player player, Event event, int basePoints, 
                                               int chain, int streak, int currentScore) {
        Map<String, String> vars = new HashMap<>();
        
        // Base points
        vars.put("base", String.valueOf(basePoints));
        
        // Player stats
        vars.put("health", String.valueOf((int) player.getHealth()));
        vars.put("max_health", String.valueOf((int) player.getMaxHealth()));
        vars.put("hunger", String.valueOf(player.getFoodLevel()));
        vars.put("level", String.valueOf(player.getLevel()));
        vars.put("exp", String.valueOf(player.getTotalExperience()));
        
        // Location
        vars.put("x", String.valueOf(player.getLocation().getBlockX()));
        vars.put("y", String.valueOf(player.getLocation().getBlockY()));
        vars.put("z", String.valueOf(player.getLocation().getBlockZ()));
        
        // Contest stats
        vars.put("score", String.valueOf(currentScore));
        vars.put("chain", String.valueOf(chain));
        vars.put("streak", String.valueOf(streak));
        
        return vars;
    }
    
    /**
     * Simple expression evaluator supporting +, -, *, /, (, )
     * Uses Dijkstra's Shunting Yard algorithm
     */
    private double evaluateExpression(String expression) {
        // Remove whitespace
        expression = expression.replaceAll("\\s+", "");
        
        // Convert to postfix notation
        java.util.Queue<String> postfix = toPostfix(expression);
        
        // Evaluate postfix expression
        java.util.Stack<Double> stack = new java.util.Stack<>();
        
        while (!postfix.isEmpty()) {
            String token = postfix.poll();
            
            if (isOperator(token)) {
                double b = stack.pop();
                double a = stack.pop();
                stack.push(applyOperator(token, a, b));
            } else {
                stack.push(Double.parseDouble(token));
            }
        }
        
        return stack.pop();
    }
    
    /**
     * Convert infix expression to postfix using Shunting Yard algorithm
     */
    private java.util.Queue<String> toPostfix(String expression) {
        java.util.Queue<String> output = new java.util.LinkedList<>();
        java.util.Stack<String> operators = new java.util.Stack<>();
        
        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);
            
            if (Character.isDigit(c) || c == '.') {
                // Number
                StringBuilder num = new StringBuilder();
                while (i < expression.length() && 
                       (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                output.add(num.toString());
                continue;
            } else if (c == '(') {
                operators.push("(");
            } else if (c == ')') {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                if (!operators.isEmpty()) {
                    operators.pop(); // Remove '('
                }
            } else if (isOperator(String.valueOf(c))) {
                while (!operators.isEmpty() && 
                       !operators.peek().equals("(") && 
                       precedence(operators.peek()) >= precedence(String.valueOf(c))) {
                    output.add(operators.pop());
                }
                operators.push(String.valueOf(c));
            }
            
            i++;
        }
        
        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }
        
        return output;
    }
    
    private boolean isOperator(String s) {
        return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("%");
    }
    
    private int precedence(String op) {
        switch (op) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
            case "%":
                return 2;
            default:
                return 0;
        }
    }
    
    private double applyOperator(String op, double a, double b) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                return b != 0 ? a / b : 0;
            case "%":
                return a % b;
            default:
                return 0;
        }
    }
}
