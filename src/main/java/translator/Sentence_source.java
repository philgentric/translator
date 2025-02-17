package translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
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
        try
        {
            props.load(new FileInputStream(in));
            Set<String> keyset = props.stringPropertyNames();
            keys = List.copyOf(keyset);

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
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
