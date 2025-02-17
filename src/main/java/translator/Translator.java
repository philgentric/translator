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

        //https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html

        List<Locale> locales = new ArrayList<>();

        //register(Locale.of("de", "DE"), locales);
        //register(Locale.of("it", "IT"), locales);
        register(Locale.of("zh", "CN"), locales);
        register(Locale.of("es", "ES"), locales);
        register(Locale.of("pt", "PT"), locales);
        register(Locale.of("ja", "JP"), locales);
        register(Locale.of("ko", "KR"), locales);

        ChatLanguageModel model = Ollama.get_model("deepseek-r1:70b");

        for (Locale locale : locales)
        {
            translate_to(locale,model);
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
    private static void translate_to(Locale locale, ChatLanguageModel model)
    //**********************************************************
    {
        Sentence_source sentence_source = new Sentence_source(new File("../klik/src/main/resources/klik/MessagesBundle_en_US.properties"));

        String name = locale.getDisplayName();
        String filename = "MessagesBundle_"+locale.getLanguage()+"_"+locale.getCountry()+".properties";

        System.out.println("\n\n\ntranslating "+name+" "+filename);


        Sentence_sink sink = new Sentence_sink(new File("../klik/src/main/resources/klik/"+filename));

        for(;;)
        {
            Pair kv = sentence_source.get_sentence();
            if ( kv == null) break;
            String prompt = "Please translate in "+name+" the following item, knowing that it is part of " +
                    "the user interface of a software application (for example, the text of a menu item) " +
                    "which is a file browser with a strong focus on images. " +
                    "Please format your output as JSON with 3 items: " +
                    "1. 'input' to repeat the input string" +
                    "2. 'translation' with the translation" +
                    "3. and 'explanations' with your comments, if any, exclusively in english.\n" +
            "Example    :"+
                    "User: ->Delete<-"+
                    "Assistant:" +
                    "{" +
                    "  \"input\": \"Delete\","+
                    "  \"translation\": \"Elimina\"," +
                    "  \"explanation\": \"...bla bla bla...\"}" +
                    "Thank you. The item to translate is ->"+kv.value()+"<-";

            String answer = model.generate(prompt);
            System.out.println("\n\n"+prompt+ " ==> " +answer+"\n\n");

            String preprocessed_answer = answer;
            String explanation = null;
            String THINK_END = "</think>";
            int kk = answer.indexOf(THINK_END);
            if ( kk > 0)
            {
                System.out.println("kk="+kk);
                preprocessed_answer = answer.substring(kk+THINK_END.length());
                explanation = answer.substring(0,kk);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(preprocessed_answer);
                String translation = String.valueOf(jsonNode.get("translation"));
                String explanation2 = String.valueOf(jsonNode.get("explanation"));
                if ( translation != null)
                {
                    translation = translation.replaceAll("\"","");
                    System.out.println("->"+kv.value()+ "<- becomes ->"+translation+"<-");
                    sink.add(kv.key(),translation);
                    sink.save();
                }
                if ( explanation2 != null) explanation = explanation2;
                System.out.println("\nExplanation: "+explanation+"\n\n");


            } catch (IOException e) {
                System.out.println("ERROR: Could not parse json: " + answer);
            }

        }

    }
}
