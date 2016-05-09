package com.jspxcms.common.word;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class WordUtils {
	public static int[] buildTableCellEdgesArray(Table table) {
		Set<Integer> edges = new TreeSet<Integer>();

		for (int r = 0; r < table.numRows(); r++) {
			TableRow tableRow = table.getRow(r);
			for (int c = 0; c < tableRow.numCells(); c++) {
				TableCell tableCell = tableRow.getCell(c);

				edges.add(Integer.valueOf(tableCell.getLeftEdge()));
				edges.add(Integer.valueOf(tableCell.getLeftEdge()
						+ tableCell.getWidth()));
			}
		}

		Integer[] sorted = edges.toArray(new Integer[edges.size()]);
		int[] result = new int[sorted.length];
		for (int i = 0; i < sorted.length; i++) {
			result[i] = sorted[i].intValue();
		}

		return result;
	}

	public static void compactSpans(Element pElement) {
		compactChildNodesR(pElement, "span");
	}

	static void compactChildNodesR(Element parentElement, String childTagName) {
		NodeList childNodes = parentElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength() - 1; i++) {
			Node child1 = childNodes.item(i);
			Node child2 = childNodes.item(i + 1);
			if (!canBeMerged(child1, child2, childTagName))
				continue;

			// merge
			while (child2.getChildNodes().getLength() > 0)
				child1.appendChild(child2.getFirstChild());
			child2.getParentNode().removeChild(child2);
			i--;
		}

		childNodes = parentElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength() - 1; i++) {
			Node child = childNodes.item(i);
			if (child instanceof Element) {
				compactChildNodesR((Element) child, childTagName);
			}
		}
	}

	static boolean canBeMerged(Node node1, Node node2, String requiredTagName) {
		if (node1.getNodeType() != Node.ELEMENT_NODE
				|| node2.getNodeType() != Node.ELEMENT_NODE)
			return false;

		Element element1 = (Element) node1;
		Element element2 = (Element) node2;

		if (!StringUtils.equals(requiredTagName, element1.getTagName())
				|| !StringUtils.equals(requiredTagName, element2.getTagName()))
			return false;

		NamedNodeMap attributes1 = element1.getAttributes();
		NamedNodeMap attributes2 = element2.getAttributes();

		if (attributes1.getLength() != attributes2.getLength())
			return false;

		for (int i = 0; i < attributes1.getLength(); i++) {
			final Attr attr1 = (Attr) attributes1.item(i);
			final Attr attr2;
			if (StringUtils.isNotEmpty(attr1.getNamespaceURI()))
				attr2 = (Attr) attributes2.getNamedItemNS(
						attr1.getNamespaceURI(), attr1.getLocalName());
			else
				attr2 = (Attr) attributes2.getNamedItem(attr1.getName());

			if (attr2 == null
					|| !StringUtils.equals(attr1.getTextContent(),
							attr2.getTextContent()))
				return false;
		}

		return true;
	}
}
