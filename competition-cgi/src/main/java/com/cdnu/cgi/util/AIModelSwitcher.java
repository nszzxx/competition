package com.cdnu.cgi.util;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * AI模型切换工具
 * 用于在不同AI模型之间快速切换配置
 */
public class AIModelSwitcher {
    
    private static final String CONFIG_FILE = "src/main/resources/application.properties";
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("使用方法: java AIModelSwitcher <模型名称>");
            System.out.println("支持的模型: openai, claude, deepseek, ollama");
            return;
        }
        
        String targetModel = args[0].toLowerCase();
        
        try {
            switchToModel(targetModel);
            System.out.println("✅ 已成功切换到 " + targetModel + " 模型");
            System.out.println("请重启应用使配置生效");
        } catch (Exception e) {
            System.err.println("❌ 切换失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换到指定的AI模型
     */
    public static void switchToModel(String targetModel) throws IOException {
        Path configPath = Paths.get(CONFIG_FILE);
        
        if (!Files.exists(configPath)) {
            throw new IOException("配置文件不存在: " + CONFIG_FILE);
        }
        
        List<String> lines = Files.readAllLines(configPath);
        
        // 处理每一行配置
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            
            // 处理ai.provider配置
            if (line.startsWith("ai.provider=") || line.startsWith("# ai.provider=")) {
                lines.set(i, "ai.provider=" + targetModel);
                continue;
            }
            
            // 处理各模型的配置行
            if (isModelConfigLine(line)) {
                String modelName = extractModelName(line);
                
                if (modelName.equals(targetModel)) {
                    // 启用目标模型的配置
                    if (line.startsWith("# ")) {
                        lines.set(i, line.substring(2)); // 移除注释
                    }
                } else {
                    // 注释其他模型的配置
                    if (!line.startsWith("# ")) {
                        lines.set(i, "# " + line); // 添加注释
                    }
                }
            }
        }
        
        // 写回文件
        Files.write(configPath, lines);
    }
    
    /**
     * 判断是否是模型配置行
     */
    private static boolean isModelConfigLine(String line) {
        String cleanLine = line.startsWith("# ") ? line.substring(2) : line;
        
        return cleanLine.startsWith("ai.openai.") ||
               cleanLine.startsWith("ai.claude.") ||
               cleanLine.startsWith("ai.deepseek.") ||
               cleanLine.startsWith("ai.ollama.");
    }
    
    /**
     * 从配置行中提取模型名称
     */
    private static String extractModelName(String line) {
        String cleanLine = line.startsWith("# ") ? line.substring(2) : line;
        
        if (cleanLine.startsWith("ai.openai.")) return "openai";
        if (cleanLine.startsWith("ai.claude.")) return "claude";
        if (cleanLine.startsWith("ai.deepseek.")) return "deepseek";
        if (cleanLine.startsWith("ai.ollama.")) return "ollama";
        
        return "";
    }
}