
# translator #

 Use LLM to translate resource bundles (java i18n)

In java, a standard way to implement 
internationalization is to leverage the Locale class,
and the Properties KV-store with its simple file format.

Assuming you have implemented it with one bundle file
e.g. MessagesBundle_en_US.properties, this translator enables to
call a LLM via langchain4j and translate your UI into many languages,
creating one new resource bundle file per language.

It uses ollama only, but leveraging langchain4j, you should be able to use many other LLMs ...

Note that it works in incremental mode so you can kill it and restart another day
which is super useful since when executing the LLM on a small machine
the process may be quite slow.
