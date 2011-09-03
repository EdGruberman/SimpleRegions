package edgruberman.java;

import java.util.Locale;

public class CaseInsensitiveString {
    
    private final Locale locale;
    private final String original;
    private final String lower;
    
    public CaseInsensitiveString(final String original) {
        this(original, Locale.getDefault());
    }
    
    public CaseInsensitiveString(final String original, final Locale locale) {
        this.locale = locale;
        this.original = original;
        this.lower = (this.original == null ? null : this.original.toLowerCase(this.locale));
    }
    
    @Override
    public String toString() {
        return this.original;
    }
    
    @Override
    public int hashCode() {
        return (this.lower == null ? 0 : this.lower.hashCode());
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null && this.original == null) return true;
        
        if (this.getClass() == other.getClass()) {
            CaseInsensitiveString that = (CaseInsensitiveString) other;
            if (this.lower.equals(that.lower)) return true;
        }
        
        if (!this.lower.equals(other.toString().toLowerCase(this.locale))) return false;
        
        return true;
    }
}