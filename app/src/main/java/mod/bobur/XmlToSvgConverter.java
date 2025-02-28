package mod.bobur;

import static com.besome.sketch.design.DesignActivity.sc_id;

import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bobur.androidsvg.SVG;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import a.a.a.wq;
import pro.sketchware.SketchApplication;
import pro.sketchware.activities.coloreditor.ColorEditorActivity;
import pro.sketchware.utility.FileUtil;

/**
 * This class is converts XML vector drawables to SVG (Only vector drawables are supported)
 **/

public class XmlToSvgConverter {

    public static String xml2svg(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xmlContent.getBytes()));

            StringWriter svg = new StringWriter();
            svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ");

            Element root = document.getDocumentElement();
            String width = root.getAttribute("android:width");
            String height = root.getAttribute("android:height");
            String viewportWidth = root.getAttribute("android:viewportWidth");
            String viewportHeight = root.getAttribute("android:viewportHeight");

            if (root.getTagName().equals("vector")) {
                if (!width.isEmpty() || !height.isEmpty()) {
                    if (parseNumber(width) > 40 || parseNumber(height) > 40) {
                        svg.append("width=\"").append(width.isEmpty() ? "150" : width).append("px\" ");
                        svg.append("height=\"").append(height.isEmpty() ? "150" : height).append("px\" ");
                    } else {
                        svg.append("width=\"150").append("px\" ");
                        svg.append("height=\"150").append("px\" ");
                    }
                } else {
                    svg.append("width=\"150").append("px\" ");
                    svg.append("height=\"150").append("px\" ");
                }
                svg.append("viewBox=\"0 0 ").append(viewportWidth.isEmpty() ? "100" : viewportWidth).append(" ").append(viewportHeight.isEmpty() ? "100" : viewportHeight).append("\" ");
            } else {
                return "NOT_SUPPORTED_YET";
            }
            svg.append(">\n");
            processElement(root, svg);
            svg.append("</svg>");
            return svg.toString();

        } catch (Exception e) {
            return "NOT_SUPPORTED";
        }
    }

    private static double parseNumber(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0;
        }
        return Double.parseDouble(input.replaceAll("[^0-9.]", ""));
    }

    private static void processElement(Element element, StringWriter svg) {
        String tagName = element.getTagName();

        switch (tagName) {
            case "group":
                handleGroup(element, svg);
                break;
            case "path":
                handlePath(element, svg);
                break;
            default:
                svg.append("<!-- Unsupported tag: ").append(tagName).append(" -->\n");
                break;
        }

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                processElement((Element) child, svg);
            }
        }
    }

    private static void handleGroup(Element group, StringWriter svg) {
        svg.append("<g ");
        String rotation = group.getAttribute("android:rotation");
        String pivotX = group.getAttribute("android:pivotX");
        String pivotY = group.getAttribute("android:pivotY");
        String scaleX = group.getAttribute("android:scaleX");
        String scaleY = group.getAttribute("android:scaleY");
        String translateX = group.getAttribute("android:translateX");
        String translateY = group.getAttribute("android:translateY");

        if (!rotation.isEmpty() || !scaleX.isEmpty() || !scaleY.isEmpty() ||
                !translateX.isEmpty() || !translateY.isEmpty()) {
            svg.append("transform=\"");
            if (!translateX.isEmpty() || !translateY.isEmpty()) {
                svg.append("translate(").append(translateX.isEmpty() ? "0" : translateX).append(",");
                svg.append(translateY.isEmpty() ? "0" : translateY).append(") ");
            }
            if (!scaleX.isEmpty() || !scaleY.isEmpty()) {
                svg.append("scale(").append(scaleX.isEmpty() ? "1" : scaleX).append(",");
                svg.append(scaleY.isEmpty() ? "1" : scaleY).append(") ");
            }
            if (!rotation.isEmpty()) {
                svg.append("rotate(").append(rotation);
                if (!pivotX.isEmpty() && !pivotY.isEmpty()) {
                    svg.append(" ").append(pivotX).append(" ").append(pivotY);
                }
                svg.append(") ");
            }
            svg.append("\" ");
        }

        svg.append(">\n");
        NodeList children = group.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                processElement((Element) child, svg);
            }
        }
        svg.append("</g>\n");
    }

    private static void handlePath(Element path, StringWriter svg) {
        String pathData = path.getAttribute("android:pathData");
        String fillColor = getVectorColor(path);
        String strokeColor = path.getAttribute("android:strokeColor");
        String strokeWidth = path.getAttribute("android:strokeWidth");
        String strokeLineCap = path.getAttribute("android:strokeLineCap");
        String strokeLineJoin = path.getAttribute("android:strokeLineJoin");
        String strokeMiterLimit = path.getAttribute("android:strokeMiterLimit");
        String fillAlpha = path.getAttribute("android:fillAlpha");
        String strokeAlpha = path.getAttribute("android:strokeAlpha");

        svg.append("<path d=\"").append(pathData).append("\" ");

        if (!fillColor.isEmpty()) {
            svg.append("fill=\"").append(convertHexColor(fillColor, path)).append("\" ");
            if (!fillAlpha.isEmpty()) {
                svg.append("fill-opacity=\"").append(fillAlpha).append("\" ");
            }
        } else {
            svg.append("fill=\"none\" ");
        }

        if (!strokeColor.isEmpty()) {
            svg.append("stroke=\"").append(convertHexColor(strokeColor, path)).append("\" ");
            if (!strokeWidth.isEmpty()) {
                svg.append("stroke-width=\"").append(parseDimension(strokeWidth)).append("\" ");
            }
            if (!strokeAlpha.isEmpty()) {
                svg.append("stroke-opacity=\"").append(strokeAlpha).append("\" ");
            }
            if (!strokeLineCap.isEmpty()) {
                svg.append("stroke-linecap=\"").append(strokeLineCap.toLowerCase()).append("\" ");
            }
            if (!strokeLineJoin.isEmpty()) {
                svg.append("stroke-linejoin=\"").append(strokeLineJoin.toLowerCase()).append("\" ");
            }
            if (!strokeMiterLimit.isEmpty()) {
                svg.append("stroke-miterlimit=\"").append(strokeMiterLimit).append("\" ");
            }
        }

        svg.append("/>\n");
    }

    public static ArrayList<String> getVectorDrawables(String sc_id) {
        ArrayList<String> cache = new ArrayList<>();
        FileUtil.listDir(wq.b(sc_id) + "/files/resource/drawable/", cache);
        cache.sort(Comparator.comparingLong(path -> new File(path).lastModified()));
        ArrayList<String> files = new ArrayList<>();
        for (String vectorPath : cache) {
            String fileName = Uri.parse(vectorPath).getLastPathSegment();
            if (fileName != null && fileName.endsWith(".xml")) {
                try {
                    String content = FileUtil.readFile(vectorPath);
                    if (content.contains("<vector")) {
                        files.add(fileName.substring(0, fileName.length() - 4));
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return files;
    }

    public static String getVectorFullPath(String sc_id, String fileName) {
        return wq.b(sc_id) + "/files/resource/drawable/" + fileName + ".xml";
    }

    private static String parseDimension(String value) {
        return value.replaceAll("[^\\d.]", "");
    }

    private static String convertHexColor(String argb, Element vectorElement) {
        if (argb.startsWith("@") || argb.startsWith("?")) {
            return getVectorColor(vectorElement);
        }

        String digits = argb.replaceAll("^#", "");
        if (digits.length() != 4 && digits.length() != 8) return argb;

        String red, green, blue, alpha;
        if (digits.length() == 4) {
            alpha = String.valueOf(digits.charAt(0));
            red = String.valueOf(digits.charAt(1));
            green = String.valueOf(digits.charAt(2));
            blue = String.valueOf(digits.charAt(3));
        } else {
            alpha = digits.substring(0, 2);
            red = digits.substring(2, 4);
            green = digits.substring(4, 6);
            blue = digits.substring(6, 8);
        }
        return "#" + red + green + blue + alpha;
    }

    public static String getVectorColor(Element vectorElement) {
        Element root = vectorElement.getOwnerDocument().getDocumentElement();
        String tint = root.getAttribute("android:tint");
        // check colors file
        String filePath = wq.b(sc_id) + "/files/resource/values/colors.xml";
        if (!FileUtil.isExistFile(filePath)) {
            filePath = wq.d(sc_id) + "/app/src/main/res/values/colors.xml";
            if (!FileUtil.isExistFile(filePath)) {
                return "#FFFFFF";
            }
        }
        ColorEditorActivity.contentPath = filePath;
        if (!tint.isEmpty()) {
            return ColorEditorActivity.getColorValue(SketchApplication.getContext(), tint, 4);
        } else {
            String fillColor = vectorElement.getAttribute("android:fillColor");
            return ColorEditorActivity.getColorValue(SketchApplication.getContext(), fillColor, 4);
        }
    }

    public static void setImageVectorFromFile(ImageView imageView, String filePath) throws Exception {
        SVG svg = SVG.getFromString(XmlToSvgConverter.xml2svg(FileUtil.readFile(filePath)));
        Picture picture = svg.renderToPicture();
        imageView.setImageDrawable(new PictureDrawable(picture));
    }
}
