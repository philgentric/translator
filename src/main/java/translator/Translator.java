package translator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//**********************************************************
public class Translator
//**********************************************************
{
    /*
    https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes
    https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2

    en	English

    zh	Chinese
    fr	French
    de	German
    it	Italian
    ja	Japanese
    ko	Korean
    es	Spanish

    pt	Portuguese
    ar	Arabic
    nl	Dutch
    ru	Russian
    ga	Irish
    br	Breton


     */


    //**********************************************************
    public static void main(String[] args)
    //**********************************************************
    {
        // change here to set were are your resources files:
        String ressources_path = "../klik/src/main/resources/klik/";


        //https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html

        List<Locale> locales = new ArrayList<>();

        // add/remove languages here:
        register(Locale.of("fr", "FR"), locales);
        register(Locale.of("de", "DE"), locales);
        register(Locale.of("it", "IT"), locales);
        register(Locale.of("zh", "CN"), locales);
        register(Locale.of("es", "ES"), locales);
        register(Locale.of("pt", "PT"), locales);
        register(Locale.of("ja", "JP"), locales);
        register(Locale.of("ko", "KR"), locales);
        register(Locale.of("br", "FR"), locales);

        ChatLanguageModel model = Ollama.get_model("deepseek-r1:70b");

        for (Locale locale : locales)
        {
            translate_to(ressources_path,locale,model);
        }


    }

    //**********************************************************
    private static void register(Locale locale, List<Locale> locales)
    //**********************************************************
    {
        System.out.println("country:"+ locale.getCountry());
        System.out.println("name:"+ locale.getDisplayName());
        System.out.println("language:"+ locale.getLanguage());
        locales.add(locale);
    }

    //**********************************************************
    private static void translate_to(String ressources_path, Locale locale, ChatLanguageModel model)
    //**********************************************************
    {
        Sentence_source sentence_source = new Sentence_source(new File(ressources_path +"MessagesBundle_en_US.properties"));

        String target_language_name = locale.getDisplayName();
        String filename = "MessagesBundle_"+locale.getLanguage()+"_"+locale.getCountry()+".properties";

        System.out.println("\n\n\ntranslating into: "+target_language_name+", the target properties file name is: "+filename);

        Sentence_sink sentence_sink = new Sentence_sink(new File(ressources_path +filename));
        for(;;)
        {
            Pair kv = sentence_source.get_sentence();
            if ( kv == null) break;

            String translated = sentence_sink.get(kv.key());
            if( translated != null)
            {
                System.out.println("Skipping key ->"+kv.key()+"<- original ->"+kv.value()+"<- is already translated as: ->"+translated+"<-");
                continue;
            }


            String prompt = "You are a smart assistant, a translator with a strong expertise in User Interfaces. " +
                    "Please translate in "+target_language_name+" the following item, knowing that it is part of " +
                    "the user interface of a software application (for example, the text of a menu item) " +
                    "which is a file browser with a strong focus on images. " +
                    "It is imperative that you format your output as JSON with 3 items: " +
                    "1. 'input' to repeat the input string" +
                    "2. 'translation' with the translation" +
                    "3. 'explanation' with your comments, if any, exclusively in english.\n" +
                    "###### begin example (english to italian) ######\n" +
                    "{" +
                    "  \"input\": \"Delete\",\n"+
                    "  \"translation\": \"Elimina\",\n" +
                    "  \"explanation\": \"Elimina is the proper translation of delete in italian\"\n}\n" +
                    "###### end example ######\n" +
                    "User: The item to translate is ->"+kv.value()+"<-\n";

            System.out.println("\n\ncalling LLM for ->"+kv.value()+"<- in "+target_language_name);
            String answer = model.generate(prompt);
            System.out.println("\n\n"+prompt+ " ==> " +answer+"\n\n");

            answer = remove_think(answer);
            answer = remove_triple_backtick(answer);


            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(answer);
                String translation = String.valueOf(jsonNode.get("translation"));
                String explanation = String.valueOf(jsonNode.get("explanation"));
                if ( translation != null)
                {
                    translation = translation.replaceAll("\"","");
                    System.out.println("->"+kv.value()+ "<- becomes ->"+translation+"<-");
                    sentence_sink.add(kv.key(),translation);
                    sentence_sink.save();
                }
                System.out.println("\nExplanation: "+explanation+"\n\n");


            } catch (IOException e) {
                System.out.println("ERROR: Could not parse json: " + answer);
            }

        }

    }

    private static String remove_think(String answer)
    {
        String THINK_END = "</think>";
        int kk = answer.indexOf(THINK_END);
        if ( kk > 0)
        {
            //System.out.println("index of think section is="+kk);
            System.out.println("Removed this <think> section:"+answer.substring(0,kk));
            return answer.substring(kk+THINK_END.length());
        }
        System.out.println("No <think> section found");
        return answer;
    }
    private static String remove_triple_backtick(String answer)
    {
        answer = answer.replaceAll("```","");

        String JSON = "json";
        int kk = answer.indexOf(JSON);
        if ( kk >= 0)
        {
            System.out.println("index of json="+kk);
            return answer.substring(kk+JSON.length());
        }
        System.out.println("no 'json' keyword");

        return answer;
    }
}
