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
    private final static boolean dbg = true;
    private final static boolean show_skipping = false;

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
        String ressources_path = "../klikr/src/main/resources/languages/";


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
        register(Locale.of("br", "FR"), locales);
        register(Locale.of("ko", "KR"), locales);

        String model_name = "granite4.1:3b";

        ChatLanguageModel model = Ollama.get_model(model_name);

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

    private final static boolean force_redo_abrevs = false;
    //**********************************************************
    private static void translate_to(String ressources_path, Locale locale, ChatLanguageModel model)
    //**********************************************************
    {
        Sentence_source sentence_source = new Sentence_source(new File(ressources_path +"MessagesBundle_en_US.properties"));

        String target_language_name = locale.getDisplayName();
        String filename = "MessagesBundle_"+locale.getLanguage()+"_"+locale.getCountry()+".properties";

        System.out.println("\n\n\ntranslating into: "+target_language_name+", the target properties file name is: "+filename);

        Sentence_sink sentence_sink = new Sentence_sink(new File(ressources_path +filename));
        int skipped = 0;
        for(;;)
        {
            Pair kv = sentence_source.get_sentence();
            if ( kv == null) break;

            if ( force_redo_abrevs)
            {
                if (kv.key().contains("Abbreviation_For_"))
                {
                    translate_one(model, target_language_name, kv, sentence_sink, true);
                    continue;
                }
            }

            String translated = sentence_sink.get(kv.key());
            if( translated == null)
            {
                translate_one(model, target_language_name, kv, sentence_sink,false);
            }
            else
            {
                if (show_skipping) System.out.println("Skipping key ->"+kv.key()+"<- original ->"+kv.value()+"<- is already translated as: ->"+translated+"<-");
                skipped++;
            }
        }
        System.out.println("Already translated items skipped: "+skipped);
    }

    //**********************************************************
    private static void translate_one(
            ChatLanguageModel model,
            String target_language_name,
            Pair kv,
            Sentence_sink sentence_sink,
            boolean abbreviation)
    //**********************************************************
    {
        String prompt = "You are a smart assistant, a translator with a strong expertise in User Interfaces. " +
                "Please translate in " + target_language_name + " the following item, written in english, knowing that it is part of " +
                "the user interface of a software application (for example, the text of a menu item) " +
                "which is a file browser with a strong focus on images. ";
        if (abbreviation)
        {
            prompt += "The item to translate is an abbreviation, so please give the actual abbreviation in the translation. " +
                    "Abbreviations are language dependant. For example in english a Billion is abbreviated as 'B' " +
                    "while in French it is abbreviated as 'Md' for 'Milliard'. "+
                    "Another example is: in english the abbreviation for 'hour' is 'h'. " +
                    "Another example is: in french the abbreviation for 'day' is 'j' since 'day' in French is 'jour'"+
                    "Do NOT reply 'abbreviation ...' " +
                    "nor '->Abbrév. pour j.<-"+
                    "but ONLY give the actual abbreviation e.g. in the last example, reply: 'j'" +

                    "It is imperative that you format your output as JSON with 3 items: " +
                            "1. 'input' to repeat the input string" +
                            "2. 'translation' with the translation" +
                            "3. 'explanation' with your comments, if any, exclusively in english.\n" +
                            "###### begin example when translating from english to french ######\n" +
                            "user input: the key is 'Abbreviation_For_Day', the value is 'd'\n" +
                            "agent reply:\n"+
                            "{" +
                            "  \"input\": \"d\",\n" +
                            "  \"translation\": \"j\",\n" +
                            "  \"explanation\": \"'j' is the proper translation in french of 'd' for 'Day' in english\"\n}\n" +
                            "###### end example ######\n" +

                    "In general, abbreviation for time units are found in time tables for trains and planes etc\n"+
                    "To help understanding what is being translated, the KEY is provided (this is the string the developer used in the code), " +
                    "The key is: " + kv.key() + ". " +
                    "The english abbreviation to be translated is: " + kv.value() + ". ";

        }
        else
        {
            prompt +=
                    "Translate the item in the shortest but clearest possible way, " +
                            "as is best for a menu item or a button in a user interface. ";

            prompt +=
                    "It is imperative that you format your output as JSON with 3 items: " +
                            "1. 'input' to repeat the input string" +
                            "2. 'translation' with the translation" +
                            "3. 'explanation' with your comments, if any, exclusively in english.\n" +
                            "###### begin example when translating from english to italian ######\n" +
                            "{" +
                            "  \"input\": \"Delete\",\n" +
                            "  \"translation\": \"Elimina\",\n" +
                            "  \"explanation\": \"Elimina is the proper translation of Delete in italian\"\n}\n" +
                            "###### end example ######\n" +
                            "User: The item to translate is ->" + kv.value() + "<-\n";
        }
        prompt += " - Ensure all JSON is **strictly valid**."+
                " - If using Unicode escapes, they **must be exactly 4 hex digits** (e.g., `\\uac00`, **not** `\\uc00`)."+
                " - Better yet: **omit escapes entirely** and use UTF-8 characters directly (Korean is fully supported in JSON)."+
                " - Double-check that your output parses as valid JSON before returning.";

        System.out.println("\ncalling LLM for ->"+ kv.key()+"<- = ->"+ kv.value()+"<- in "+ target_language_name+"\n"+prompt);
        String answer;
        try {
            answer = model.generate(prompt);
        }
        catch (RuntimeException e)
        {
            System.out.println("\n\n"+prompt+ " FAILED " +e+"\n\n");
            return;
        }
        //System.out.println("\n\n"+prompt+ " ==> " +answer+"\n\n");
        System.out.println(kv.value()+ " translated ==> ->" +answer+ "<-");

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
                if ( dbg) System.out.println("->"+ kv.value()+ "<- becomes ->"+translation+"<-");
                sentence_sink.add(kv.key(),translation);
                sentence_sink.save();
            }
            if ( dbg)  System.out.println("\nExplanation: "+explanation+"\n\n");

        } catch (IOException e) {
            System.out.println("ERROR: Could not parse json: " + answer);
        }
    }

    //**********************************************************
    private static String remove_think(String answer)
    //**********************************************************
    {
        String THINK_END = "</think>";
        int kk = answer.indexOf(THINK_END);
        if ( kk > 0)
        {
            //System.out.println("index of think section is="+kk);
            if ( dbg) System.out.println("Removed this <think> section:"+answer.substring(0,kk));
            return answer.substring(kk+THINK_END.length());
        }
        if ( dbg) System.out.println("No <think> section found");
        return answer;
    }
    //**********************************************************
    private static String remove_triple_backtick(String answer)
    //**********************************************************
    {
        answer = answer.replaceAll("```","");

        String JSON = "json";
        int kk = answer.indexOf(JSON);
        if ( kk >= 0)
        {
            if ( dbg) System.out.println("index of json="+kk);
            return answer.substring(kk+JSON.length());
        }
        if ( dbg) System.out.println("warning: no 'json' keyword (normally OK)");

        return answer;
    }
}
