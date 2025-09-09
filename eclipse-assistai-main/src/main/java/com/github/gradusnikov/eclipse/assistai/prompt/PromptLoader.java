package com.github.gradusnikov.eclipse.assistai.prompt;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.di.annotations.Creatable;

import jakarta.inject.Singleton;

/**
 * A singleton class responsible for loading prompt text from resource files and
 * applying substitutions. The resource files are located in the "prompts"
 * folder of the plugin.
 */
@Creatable
@Singleton
public class PromptLoader {
    private final String BASE_URL = "platform:/plugin/com.github.gradusnikov.eclipse.plugin.assistai.main/prompts/";
    private String baseURL = BASE_URL;

    public PromptLoader() {
    }

    public String getDefaultPrompt(String resourceFile) {
        try (var in = FileLocator.toFileURL(buildResourceUrl(resourceFile)).openStream();
                var dis = new DataInputStream(in);) {
            var prompt = new String(dis.readAllBytes(), StandardCharsets.UTF_8);
            return prompt;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用推荐的URI方式构建资源URL，替代已弃用的URL(URL, String)构造方法
     * 
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    private URL buildResourceUrl(String resourceFile) throws IOException {
        try {
            // 使用URI.resolve()方法来安全地构建相对URL
            URI baseUri = new URI(baseURL);
            URI resourceUri = baseUri.resolve(resourceFile);
            return resourceUri.toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid base URL: " + baseURL, e);
        }
    }
}
