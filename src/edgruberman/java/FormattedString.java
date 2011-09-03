package edgruberman.java;

/**
 * Avoid processing overhead of formatting a string on each reference
 * while still retaining original format string and arguments.
 */
public class FormattedString {

    /**
     * Text of message with substitutions already performed.
     */
    public String formatted;
    
    private String format;
    private Object[] args; 

    /**
     * Create new formatted string.
     *
     * @param format format string
     * @param args arguments referenced by format specifiers
     */
    public FormattedString(final String format, final Object... args) {
        this.format = format;
        this.args = args;
        this.format();
    }

    /**
     * Configure string with specified format.
     * 
     * @param format format string
     */
    public String setFormat(final String format) {
        this.format = format;
        return this.format();
    }

    /**
     * Current format string without substitutions.
     *
     * @return current message format
     */
    public String getFormat() {
        return this.format;
    }
    
    /**
     * Configure arguments used to format string.
     *
     * @param args arguments referenced by format specifiers
     */
    public String setArgs(final Object... args) {
        this.args = args;
        return this.format();
    }
    
    /**
     * Current arguments used to format string.
     *
     * @return arguments referenced by format specifiers
     */
    public Object[] getArgs() {
        return this.args;
    }

    /**
     * The already formatted string.
     */
    @Override
    public String toString() {
        return this.formatted;
    }

    /**
     * Format string using the existing format string and the
     * specified arguments.
     *
     * @return formatted string
     */
    private String format() {
        this.formatted = String.format(this.format, this.args);
        return this.formatted;
    }
}