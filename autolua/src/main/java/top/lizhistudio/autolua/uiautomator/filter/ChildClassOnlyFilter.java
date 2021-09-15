package top.lizhistudio.autolua.uiautomator.filter;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.regex.Pattern;

abstract class ChildClassOnlyFilter implements Filter{
}


abstract class ClassNameFilter extends ChildClassOnlyFilter {
}

class ClassNameSimpleFilter extends ClassNameFilter{
    private final String name;
    ClassNameSimpleFilter(String name)
    {
        this.name = name;
    }

    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        CharSequence s = info.getClassName();
        return s!= null && name.contentEquals(s);
    }


}

class ClassNamePatternFilter extends ClassNameFilter{
    private final Pattern pattern;
    ClassNamePatternFilter(Pattern pattern)
    {
        this.pattern = pattern;
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        CharSequence value = info.getClassName();
        return pattern.matcher(value != null ? value : "").matches();
    }

    public static ClassNamePatternFilter newFilterContains(String s)
    {
        return new ClassNamePatternFilter(Pattern.compile(String.format("^.*%s.*$", Pattern.quote(s))));
    }

    public static ClassNamePatternFilter newFilterStartsWith(String s)
    {
        return new ClassNamePatternFilter(Pattern.compile(String.format("^%s.*$", Pattern.quote(s))));
    }

    public static ClassNamePatternFilter newFilterEndsWith(String s)
    {
        return new ClassNamePatternFilter(Pattern.compile(String.format("^.*%s$", Pattern.quote(s))));
    }
}


abstract class DescriptionFilter extends ChildClassOnlyFilter {
}

class DescriptionSimpleFilter extends DescriptionFilter{
    private final String value;
    DescriptionSimpleFilter(String value)
    {
        this.value = value;
    }

    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        CharSequence s = info.getContentDescription();
        return s!= null && value.contentEquals(s);
    }
}

class DescriptionPatternFilter extends DescriptionFilter{
    private final Pattern pattern;
    DescriptionPatternFilter(Pattern pattern)
    {
        this.pattern = pattern;
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        CharSequence value = info.getContentDescription();
        return pattern.matcher(value != null ? value : "").matches();
    }

    public static DescriptionPatternFilter newFilterContains(String s)
    {
        return new DescriptionPatternFilter(Pattern.compile(String.format("^.*%s.*$", Pattern.quote(s))));
    }

    public static DescriptionPatternFilter newFilterStartsWith(String s)
    {
        return new DescriptionPatternFilter(Pattern.compile(String.format("^%s.*$", Pattern.quote(s))));
    }

    public static DescriptionPatternFilter newFilterEndsWith(String s)
    {
        return new DescriptionPatternFilter(Pattern.compile(String.format("^.*%s$", Pattern.quote(s))));
    }
}


abstract class PackageNameFilter extends ChildClassOnlyFilter {
}

class PackageNameSimpleFilter extends PackageNameFilter{
    private final String name;
    PackageNameSimpleFilter(String name)
    {
        this.name = name;
    }

    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        CharSequence s = info.getPackageName();
        return s!= null && name.contentEquals(s);
    }


}

class PackageNamePatternFilter extends PackageNameFilter{
    private final Pattern pattern;
    PackageNamePatternFilter(Pattern pattern)
    {
        this.pattern = pattern;
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        CharSequence value = info.getPackageName();
        return pattern.matcher(value != null ? value : "").matches();
    }

    public static PackageNamePatternFilter newFilterContains(String s)
    {
        return new PackageNamePatternFilter(Pattern.compile(String.format("^.*%s.*$", Pattern.quote(s))));
    }

    public static PackageNamePatternFilter newFilterStartsWith(String s)
    {
        return new PackageNamePatternFilter(Pattern.compile(String.format("^%s.*$", Pattern.quote(s))));
    }

    public static PackageNamePatternFilter newFilterEndsWith(String s)
    {
        return new PackageNamePatternFilter(Pattern.compile(String.format("^.*%s$", Pattern.quote(s))));
    }
}



abstract class ResourceFilter extends  ChildClassOnlyFilter{
}

class ResourceSimpleFilter extends ResourceFilter{
    private final String value;
    ResourceSimpleFilter(String value)
    {
        this.value = value;
    }

    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        return value.equals(info.getViewIdResourceName());
    }
}

class ResourcePatternFilter extends ResourceFilter{
    private final Pattern pattern;
    ResourcePatternFilter(Pattern pattern)
    {
        this.pattern = pattern;
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        CharSequence value = info.getViewIdResourceName();
        return pattern.matcher(value != null ? value : "").matches();
    }

    public static ResourcePatternFilter newFilterContains(String s)
    {
        return new ResourcePatternFilter(Pattern.compile(String.format("^.*%s.*$", Pattern.quote(s))));
    }

    public static ResourcePatternFilter newFilterStartsWith(String s)
    {
        return new ResourcePatternFilter(Pattern.compile(String.format("^%s.*$", Pattern.quote(s))));
    }

    public static ResourcePatternFilter newFilterEndsWith(String s)
    {
        return new ResourcePatternFilter(Pattern.compile(String.format("^.*%s$", Pattern.quote(s))));
    }
}


abstract class TextFilter extends ChildClassOnlyFilter {
}

class TextSimpleFilter extends TextFilter{
    private final String text;
    TextSimpleFilter(String text)
    {
        this.text = text;
    }

    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        CharSequence s = info.getText();
        return s!= null && text.contentEquals(s);
    }
}

class TextPatternFilter extends TextFilter{
    private final Pattern pattern;
    TextPatternFilter(Pattern pattern)
    {
        this.pattern = pattern;
    }
    @Override
    public boolean isMatch(AccessibilityNodeInfo info) {
        CharSequence value = info.getText();
        return pattern.matcher(value != null ? value : "").matches();
    }

    public static TextPatternFilter newFilterContains(String s)
    {
        return new TextPatternFilter(Pattern.compile(String.format("^.*%s.*$", Pattern.quote(s))));
    }

    public static TextPatternFilter newFilterStartsWith(String s)
    {
        return new TextPatternFilter(Pattern.compile(String.format("^%s.*$", Pattern.quote(s))));
    }

    public static TextPatternFilter newFilterEndsWith(String s)
    {
        return new TextPatternFilter(Pattern.compile(String.format("^.*%s$", Pattern.quote(s))));
    }
}



