package com.yupi.yuaiagent.demo.invoke;

/**
 * 仅用于测试获取 API Key
 * 请在环境变量中设置 DASHSCOPE_API_KEY，不要在代码里硬编码！
 */
public interface TestApiKey {

    String API_KEY = System.getenv("DASHSCOPE_API_KEY") != null
            ? System.getenv("DASHSCOPE_API_KEY")
            : "";
}
