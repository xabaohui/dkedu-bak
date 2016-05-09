package com.jspxcms.common.word;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class HtmlDocument {

	protected final Document document;
//	protected final Element html;
	protected final Element body;

	public HtmlDocument(Document document) {
		this.document = document;
//        html = document.createElement( "html" );
//        document.appendChild( html );

        body = document.createElement( "body" );
        document.appendChild( body );
	}

	public Element createDiv() {
		return document.createElement("div");
	}

	public Element createBookmark(String name) {
		final Element basicLink = document.createElement("a");
		basicLink.setAttribute("name", name);
		return basicLink;
	}

	public Element createH1() {
		return document.createElement("h1");
	}

	public Element createH2() {
		return document.createElement("h2");
	}

	public Element createSpan() {
		return document.createElement("span");
	}

	public Element createStrong() {
		return document.createElement("strong");
	}

	public Element createEm() {
		return document.createElement("em");
	}

	public Element createHyperlink(String internalDestination) {
		final Element basicLink = document.createElement("a");
		basicLink.setAttribute("href", internalDestination);
		basicLink.setAttribute("target", "_blank");
		return basicLink;
	}

	public Element createImage(String src) {
		Element result = document.createElement("img");
		result.setAttribute("src", src);
		return result;
	}

	public Element createBr() {
		return document.createElement("br");
	}

	public Element createLi() {
		return document.createElement("li");
	}

	public Element createOption(String value, boolean selected) {
		Element result = document.createElement("option");
		result.appendChild(createText(value));
		if (selected) {
			result.setAttribute("selected", "selected");
		}
		return result;
	}

	public Element createP() {
		return document.createElement("p");
	}

	public Element createSelect() {
		Element result = document.createElement("select");
		return result;
	}

	public Element createTable() {
		return document.createElement("table");
	}

	public Element createTbody() {
		return document.createElement("tbody");
	}

	public Element createTd() {
		return document.createElement("td");
	}

	public Element createCol() {
		return document.createElement("col");
	}

	public Element createColgroup() {
		return document.createElement("colgroup");
	}

	public Element createThead() {
		return document.createElement("thead");
	}

	public Element createTh() {
		return document.createElement("th");
	}

	public Element createTr() {
		return document.createElement("tr");
	}

	public Text createText(String data) {
		return document.createTextNode(data);
	}

	public Element createUl() {
		return document.createElement("ul");
	}

	public Element createOl() {
		return document.createElement("ol");
	}

	public Document getDocument() {
		return document;
	}
}
