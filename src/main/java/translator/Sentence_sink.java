package translator;

import java.io.File;
import java.util.List;
import java.util.Properties;

//**********************************************************
public class Sentence_sink
//**********************************************************
{
    Properties the_properties;
    File file_out;

    //**********************************************************
    public Sentence_sink(File file)
    //**********************************************************
    {
        this.file_out = file;
        the_properties = new Properties();

        Sentence_source.load(the_properties, file_out,false);

    }
    //**********************************************************
    public void add(String key, String value)
    //**********************************************************
    {
        the_properties.put(key,value);
    }
    //**********************************************************
    public boolean save()
    //**********************************************************
    {
        // save all properties
        try
        {
            the_properties.store(new java.io.FileOutputStream(file_out), "No comment");
            return true;
        }
        catch (java.io.IOException e)
        {
            return false;
        }

    }

    //**********************************************************
    public String get(String key)
    //**********************************************************
    {
        return the_properties.getProperty(key);
    }
}
