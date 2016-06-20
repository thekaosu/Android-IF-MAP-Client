/*
 * IfMapMetadataDocumentCreator.java        0.2 2015-03-08
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package de.simu.decoit.android.decomap.messaging;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.ifmapj.identifier.IdentityType;
import de.hshannover.f4.trust.ifmapj.metadata.Cardinality;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;
import de.simu.decoit.android.decomap.util.CryptoUtil;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Support methods to generate metadata document structures for the IfmapJ-lib
 *
 * @author Marcel Jahnke, DECOIT GmbH
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class IfMapMetadataDocumentCreator {

    private DocumentBuilder mDocumentBuilder;

    private final PreferencesValues mPreferences = PreferencesValues.getInstance();
    private final MessageParameter mp = MessageParameter.getInstance();

    private static IfMapMetadataDocumentCreator instance;

    /**
     * constructor
     */
    private IfMapMetadataDocumentCreator() {
        // create document-builder from factory
        DocumentBuilderFactory mDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            mDocumentBuilder = mDocumentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            Toolbox.logTxt(this.getClass().getName(),
                    "error while parsing parameters: " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Singelton instance getter
     *
     * @return Singelton instance of Messageparameter
     */
    public static synchronized IfMapMetadataDocumentCreator getInstance() {
        if (instance == null) {
            instance = new IfMapMetadataDocumentCreator();
        }
        return instance;
    }

    /**
     * create a standard single element document
     *
     * @param name name of the element
     * @param card cardinality of the element
     * @return single element document
     */
    public Document createStdSingleElementDocument(String name, Cardinality card) {
        return createSingleElementDocument(IfmapStrings.STD_METADATA_PREFIX + ":" + name, IfmapStrings.STD_METADATA_NS_URI, card);
    }

    /**
     * create a single element document
     *
     * @param qualifiedName qualified name of element
     * @param uri           namespace uri
     * @param cardinality   cardinality of element
     * @return single element document
     */
    private Document createSingleElementDocument(String qualifiedName, String uri, Cardinality cardinality) {
        Document doc = mDocumentBuilder.newDocument();
        Element e = doc.createElementNS(uri, qualifiedName);
        e.setAttributeNS(null, "ifmap-cardinality", cardinality.toString());
        doc.appendChild(e);
        return doc;
    }

    /**
     * create and append a text element
     *
     * @param doc    document
     * @param parent parent element
     * @param elName name of element
     * @param value  value for element
     * @return element
     */
    private Element createAndAppendTextElementCheckNull(Document doc, Element parent, String elName, Object value) {
        if (doc == null || parent == null || elName == null) {
            throw new NullPointerException("bad parameters given");
        }

        if (value == null) {
            throw new NullPointerException("null is not allowed for " + elName + " in " + doc.getFirstChild().getLocalName());
        }

        String valueStr = value.toString();
        if (valueStr == null) {
            throw new NullPointerException("null-string not allowed for " + elName + " in " + doc.getFirstChild().getLocalName());
        }

        Element child = createAndAppendElement(doc, parent, elName);
        Text txtCElement = doc.createTextNode(valueStr);
        child.appendChild(txtCElement);
        return child;
    }

    /**
     * Helper to create an {@link Element} without a namespace in {@link Document} doc and append it to the {@link Element} given by parent.
     *
     * @param doc    Document to create with
     * @param elName node to add
     * @return element parent element to add onto
     */
    private Element createAndAppendElement(Document doc, Element parent, String elName) {
        Element el = doc.createElementNS(null, elName);
        parent.appendChild(el);
        return el;
    }

    /**
     * Helper to create a new element with name elName and append it to the {@link Element} given by parent if the given value is non-null.
     * The new {@link Element} will have {@link Text} node containing value.
     *
     * @param doc    {@link Document} where parent is located in
     * @param parent where to append the new element
     * @param elName the name of the new element.
     * @param value  the value of the {@link Text} node appended to the new element, using toString() on the object.
     */
    public void appendTextElementIfNotNull(Document doc, Element parent, String elName, Object value) {
        if (value == null) {
            return;
        }
        createAndAppendTextElementCheckNull(doc, parent, elName, value);
    }

    /**
     * create feature-metadata
     *
     * @param id          content for id-element
     * @param timestamp   timestamp-string
     * @param value       content for value-element
     * @param contentType type of content
     * @return feature-metadata-document
     */
    public Document createFeature(String id, String timestamp, String value, String contentType) {
        Document doc = mDocumentBuilder.newDocument();
        Element feature = doc.createElementNS(MessageParametersGenerator.NAMESPACE, MessageParametersGenerator.NAMESPACE_PREFIX + ":feature");

        feature.setAttributeNS(null, "ifmap-cardinality", "multiValue");
        feature.setAttribute("ctxp-timestamp", timestamp);
        if (mPreferences.isEnableLocationTracking() && (mp.getLatitude() != null && mp.getLongitude() != null)) {
            feature.setAttribute("ctxp-position", mp.getLatitude() + "-" + mp.getLongitude());
        }

        Element idElement = doc.createElement("id");
        idElement.setTextContent(id);
        feature.appendChild(idElement);

        Element typeElement = doc.createElement("type");
        typeElement.setTextContent(contentType);
        feature.appendChild(typeElement);

        Element valueElement = doc.createElement("value");
        valueElement.setTextContent(value);
        feature.appendChild(valueElement);

        doc.appendChild(feature);
        return doc;
    }

    /**
     * create new category-link
     *
     * @param name category-link-name
     * @return category-link-metadata-document
     */
    public Document createCategoryLink(String name) {
        Document doc = mDocumentBuilder.newDocument();
        Element e = doc.createElementNS(MessageParametersGenerator.NAMESPACE, MessageParametersGenerator.NAMESPACE_PREFIX + ":" + name);
        e.setAttributeNS(null, "ifmap-cardinality", "singleValue");

        doc.appendChild(e);
        return doc;
    }

    /**
     * create a new category
     *
     * @param name      category-name
     * @param admDomain administrative-domain-string
     * @return category-identifer
     */
    public Identity createCategory(String name, String admDomain) {
        return Identifiers.createIdentity(IdentityType.other, name, admDomain, MessageParametersGenerator.OTHER_TYPE_DEFINITION);
    }

    /**
     * Anonymize the given input String by using a cryptographic hash.
     *
     * @param input string to encrypte
     * @return sha256 encrypted string
     */
    public String anonymize(String input) {
        String salt = CryptoUtil.sha256(input);
        // 10-times sha-256 hashing
        for (int i = 0; i < 10; i++) {
            input = CryptoUtil.sha256(input + salt);
        }
        return input;
    }
}
