package analyzer.util;

import java.util.Collection;

/**
 * String manipulation utilities
 */
public class StringUtils {

    /**
     * Concatenate  an array of elements into a string separated by separator
     * @param separator separator between each element
     * @param elements elements to be joined
     * @return
     */
    public static String join(String separator, final String[] elements) {
        if (elements == null) return null;
        final StringBuilder buf = new StringBuilder(elements.length * 16);
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                buf.append(separator);
            }
            buf.append(elements[i]);
        }
        return buf.toString();
    }

    /**
     * Concatenate  a collection of elements into a string separated by separator
     * @param separator separator between each element
     * @param elements elements to be joined
     * @return
     */
    public static String join(String separator, final Collection<String> elements) {
        if (elements == null) return null;
        final StringBuilder buf = new StringBuilder(elements.size() * 16);
        boolean first = true;
        for (String element : elements) {
            if (first)
                first = false;
            else
                buf.append(separator);
            buf.append(element);
        }
        return buf.toString();
    }
}
