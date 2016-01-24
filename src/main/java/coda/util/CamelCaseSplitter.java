package coda.util;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class CamelCaseSplitter {

    //  http://stackoverflow.com/a/7599674
    private static final String REGEX = "(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])";

    public static String[] split(String input) {
        return input.split(REGEX);
    }
}
