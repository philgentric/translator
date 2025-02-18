package translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

//**********************************************************
public class Sentence_source
//**********************************************************
{
    Properties props;
    List<String> keys;
    int current_key = 0;

    //**********************************************************
    public Sentence_source(File in)
    //**********************************************************
    {
        props = new Properties();
        keys = load(props, in, true);
    }

    //**********************************************************
    public static List<String> load(Properties props, File in, boolean no_file_is_fatal)
    //**********************************************************
    {
        try
        {
            props.load(new FileInputStream(in));
            Set<String> keyset = props.stringPropertyNames();
            return List.copyOf(keyset);
        }
        catch (IOException e)
        {
            if ( no_file_is_fatal)
            {
                System.out.println("PANIC: "+e);
                return null;
            }
            System.out.println("WARNING: "+e);
            return new ArrayList<>();
        }
    }

    //**********************************************************
    public Pair get_sentence()
    //**********************************************************
    {
        String key = keys.get(current_key++);
        if ( current_key >= keys.size()) return null;
        String value = props.getProperty(key);
        return new Pair(key, value);
    }
}
