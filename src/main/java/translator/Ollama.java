package translator;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;

//**********************************************************
public class Ollama
//**********************************************************
{
    //**********************************************************
    public static ChatLanguageModel get_model(String model_name)
    //**********************************************************
    {
        return OllamaChatModel.builder()
                              .baseUrl("http://127.0.0.1:11434/")
                              .modelName(model_name)
                              .temperature(0.0)
                              .timeout(Duration.ofMinutes(5))
                              .build();
    }
}