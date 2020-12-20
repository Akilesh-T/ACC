package app.akilesh.qacc.utils

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import app.akilesh.qacc.BuildConfig
import app.akilesh.qacc.Const.Colors.nokiaBlue
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object XmlUtils {

    private val transformerFactory: TransformerFactory by lazy { TransformerFactory.newInstance() }

    private val documentBuilder: DocumentBuilder by lazy {
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder()
        } catch (exception: ParserConfigurationException) {
            throw IllegalStateException("Unable to initiate")
        }
    }

    fun createOverlayManifest(file: File, pkgName: String, accentName: String) {
        val document = documentBuilder.newDocument()

        val rootElement = document.createElement("manifest")
        rootElement.setAttribute("xmlns:android",
            "http://schemas.android.com/apk/res/android")
        rootElement.setAttribute("package", pkgName)
        rootElement.setAttribute("android:versionName", BuildConfig.VERSION_NAME)
        rootElement.setAttribute("android:versionCode", BuildConfig.VERSION_CODE.toString())

        val overlayElement = document.createElement("overlay")
        overlayElement.setAttribute("android:priority", "1")
        overlayElement.setAttribute("android:targetPackage", "android")
        overlayElement.setAttribute("android:category", "android.theme.customization.accent_color")
        rootElement.appendChild(overlayElement)

        val applicationElement = document.createElement("application")
        applicationElement.setAttribute("android:hasCode", "false")
        applicationElement.setAttribute("android:allowBackup","false")
        applicationElement.setAttribute("android:extractNativeLibs","false")
        applicationElement.setAttribute("android:label", accentName)
        rootElement.appendChild(applicationElement)

        document.appendChild(rootElement)

        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8")
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        val source = DOMSource(document)
        transformer.transform(source, StreamResult(file))
    }

    fun createColors(file: File, colorLight: String, colorDark: String, hasNokiaBlue: Boolean) {
        val document = documentBuilder.newDocument()
        val resourcesElement = document.createElement("resources")
        addColor(document, resourcesElement, "accent_device_default_light", colorLight)
        addColor(document, resourcesElement, "accent_device_default_dark", colorDark)
        if (SDK_INT < Q) {
            addColor(document, resourcesElement, "material_blue_grey_700", colorLight)
            addColor(document, resourcesElement, "material_blue_grey_800", colorLight)
            addColor(document, resourcesElement, "material_blue_grey_900", colorLight)
            addColor(document, resourcesElement, "system_notification_accent_color", colorLight)
            addColor(document, resourcesElement, "notification_default_color", colorLight)
            addColor(document, resourcesElement, "notification_icon_bg_color", colorLight)
            addColor(document, resourcesElement, "accent_material_dark", colorLight)
            addColor(document, resourcesElement, "accent_material_light", colorLight)
            addColor(document, resourcesElement, "accent_device_default_700", colorLight)
            addColor(document, resourcesElement, "accent_device_default_50", colorLight)
            addColor(document, resourcesElement, "material_deep_teal_200", colorLight)
            addColor(document, resourcesElement, "material_deep_teal_500", colorLight)
            if (hasNokiaBlue) addColor(document, resourcesElement, nokiaBlue, colorLight)
        }
        document.appendChild(resourcesElement)

        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        val source = DOMSource(document)
        transformer.transform(source, StreamResult(file))
    }

    private fun addColor(document: Document, resourceElement: Element, name: String, value: String) {
        val element = document.createElement("color")
        element.setAttribute("name", name)
        element.appendChild(document.createTextNode(value))
        resourceElement.appendChild(element)
    }
}