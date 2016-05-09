package com.jspxcms.common.word;

import static org.apache.poi.hwpf.converter.AbstractWordUtils.TWIPS_PER_INCH;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.AbstractWordConverter;
import org.apache.poi.hwpf.converter.FontReplacer.Triplet;
import org.apache.poi.hwpf.usermodel.Bookmark;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.OfficeDrawing;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class WordToHtml extends AbstractWordConverter {
	private static final POILogger logger = POILogFactory
			.getLogger(WordToHtml.class);

	private final HtmlDocument htmlDocument;

	public WordToHtml(Document document) {
		this.htmlDocument = new HtmlDocument(document);
	}

	@Override
	public Document getDocument() {
		return htmlDocument.getDocument();
	}

	@Override
	protected void outputCharacters(Element block, CharacterRun characterRun,
			String text) {
		Element element;
		Triplet triplet = getCharacterRunTriplet(characterRun);
		if (triplet.bold) {
			element = htmlDocument.createStrong();
			block.appendChild(element);
			block = element;
		}
		if (triplet.italic) {
			element = htmlDocument.createEm();
			block.appendChild(element);
			block = element;
		}
		Text textNode = htmlDocument.createText(text);
		block.appendChild(textNode);
	}

	@Override
	protected void processBookmarks(HWPFDocumentCore wordDocument,
			Element currentBlock, Range range, int currentTableLevel,
			List<Bookmark> rangeBookmarks) {
		Element parent = currentBlock;
		for (Bookmark bookmark : rangeBookmarks) {
			Element bookmarkElement = htmlDocument.createBookmark(bookmark
					.getName());
			parent.appendChild(bookmarkElement);
			parent = bookmarkElement;
		}
		if (range != null) {
			processCharacters(wordDocument, currentTableLevel, range, parent);
		}
	}

	@Override
	protected void processDocumentInformation(
			SummaryInformation summaryInformation) {
		// do nothing
	}

	@Override
	public void processDocumentPart(HWPFDocumentCore wordDocument, Range range) {
		super.processDocumentPart(wordDocument, range);
		afterProcess();
	}

	@Override
	protected void processDropDownList(Element block,
			CharacterRun characterRun, String[] values, int defaultIndex) {
		Element select = htmlDocument.createSelect();
		for (int i = 0; i < values.length; i++) {
			select.appendChild(htmlDocument.createOption(values[i],
					defaultIndex == i));
		}
		block.appendChild(select);
	}

	@Override
	protected void processDrawnObject(HWPFDocument doc,
			CharacterRun characterRun, OfficeDrawing officeDrawing,
			String path, Element block) {
		Element img = htmlDocument.createImage(path);
		block.appendChild(img);
	}

	@Override
	protected void processEndnoteAutonumbered(HWPFDocument wordDocument,
			int noteIndex, Element block, Range endnoteTextRange) {
		processNoteAutonumbered(wordDocument, "end", noteIndex, block,
				endnoteTextRange);
	}

	@Override
	protected void processFootnoteAutonumbered(HWPFDocument wordDocument,
			int noteIndex, Element block, Range footnoteTextRange) {
		processNoteAutonumbered(wordDocument, "foot", noteIndex, block,
				footnoteTextRange);
	}

	@Override
	protected void processHyperlink(HWPFDocumentCore wordDocument,
			Element currentBlock, Range textRange, int currentTableLevel,
			String hyperlink) {
		Element basicLink = htmlDocument.createHyperlink(hyperlink);
		currentBlock.appendChild(basicLink);
		if (textRange != null) {
			processCharacters(wordDocument, currentTableLevel, textRange,
					basicLink);
		}
	}

	protected void processImage(Element currentBlock, boolean inlined,
			Picture picture, String imageSourcePath) {
		final int aspectRatioX = picture.getHorizontalScalingFactor();
		final int aspectRatioY = picture.getVerticalScalingFactor();
		final float imageWidth;
		final float imageHeight;
		if (aspectRatioX > 0) {
			imageWidth = picture.getDxaGoal() * aspectRatioX / 1000
					/ TWIPS_PER_INCH;
		} else {
			imageWidth = picture.getDxaGoal() / TWIPS_PER_INCH;
		}
		if (aspectRatioY > 0) {
			imageHeight = picture.getDyaGoal() * aspectRatioY / 1000
					/ TWIPS_PER_INCH;
		} else {
			imageHeight = picture.getDyaGoal() / TWIPS_PER_INCH;
		}
		Element root = htmlDocument.createImage(imageSourcePath);
		root.setAttribute("style", "width:" + imageWidth + "in;height:"
				+ imageHeight + "in;vertical-align:text-bottom;");
		currentBlock.appendChild(root);
	}

	@Override
	protected void processImageWithoutPicturesManager(Element currentBlock,
			boolean inlined, Picture picture) {
		// no default implementation -- skip
		currentBlock.appendChild(htmlDocument.document
				.createComment("Image link to '"
						+ picture.suggestFullFileName() + "' can be here"));
	}

	@Override
	protected void processLineBreak(Element block, CharacterRun characterRun) {
		block.appendChild(htmlDocument.createBr());
	}

	@Override
	protected void processPageBreak(HWPFDocumentCore wordDocument, Element flow) {
		flow.appendChild(htmlDocument.createBr());
	}

	@Override
	protected void processPageref(HWPFDocumentCore hwpfDocument,
			Element currentBlock, Range textRange, int currentTableLevel,
			String pageref) {
		Element basicLink = htmlDocument.createHyperlink("#" + pageref);
		currentBlock.appendChild(basicLink);
		if (textRange != null) {
			processCharacters(hwpfDocument, currentTableLevel, textRange,
					basicLink);
		}
	}

	@Override
	protected void processParagraph(HWPFDocumentCore hwpfDocument,
			Element parentElement, int currentTableLevel, Paragraph paragraph,
			String bulletText) {
		final Element pElement = htmlDocument.createP();
		parentElement.appendChild(pElement);
		final int charRuns = paragraph.numCharacterRuns();
		if (charRuns == 0) {
			return;
		}
		if (StringUtils.isNotEmpty(bulletText)) {
			Text textNode = htmlDocument.createText(bulletText.substring(0,
					bulletText.length() - 1));
			pElement.appendChild(textNode);
		}
		processCharacters(hwpfDocument, currentTableLevel, paragraph, pElement);
		WordUtils.compactSpans(pElement);
		return;
	}

	@Override
	protected void processSection(HWPFDocumentCore wordDocument,
			Section section, int sectionCounter) {
		Element div = htmlDocument.createDiv();
		htmlDocument.body.appendChild(div);
		processParagraphes(wordDocument, div, section, Integer.MIN_VALUE);
	}

	@Override
	protected void processSingleSection(HWPFDocumentCore wordDocument,
			Section section) {
		processParagraphes(wordDocument, htmlDocument.body, section,
				Integer.MIN_VALUE);
	}

	@Override
	protected void processTable(HWPFDocumentCore hwpfDocument, Element flow,
			Table table) {
		Element tableHeader = htmlDocument.createThead();
		Element tableBody = htmlDocument.createTbody();

		final int[] tableCellEdges = WordUtils.buildTableCellEdgesArray(table);
		final int tableRows = table.numRows();

		int maxColumns = Integer.MIN_VALUE;
		for (int r = 0; r < tableRows; r++) {
			maxColumns = Math.max(maxColumns, table.getRow(r).numCells());
		}

		for (int r = 0; r < tableRows; r++) {
			TableRow tableRow = table.getRow(r);

			Element tableRowElement = htmlDocument.createTr();

			// index of current element in tableCellEdges[]
			int currentEdgeIndex = 0;
			final int rowCells = tableRow.numCells();
			for (int c = 0; c < rowCells; c++) {
				TableCell tableCell = tableRow.getCell(c);

				if (tableCell.isVerticallyMerged()
						&& !tableCell.isFirstVerticallyMerged()) {
					currentEdgeIndex += getNumberColumnsSpanned(tableCellEdges,
							currentEdgeIndex, tableCell);
					continue;
				}

				Element tableCellElement;
				if (tableRow.isTableHeader()) {
					tableCellElement = htmlDocument.createTh();
				} else {
					tableCellElement = htmlDocument.createTd();
				}
				int colSpan = getNumberColumnsSpanned(tableCellEdges,
						currentEdgeIndex, tableCell);
				currentEdgeIndex += colSpan;

				if (colSpan == 0)
					continue;

				if (colSpan != 1)
					tableCellElement.setAttribute("colspan",
							String.valueOf(colSpan));

				final int rowSpan = getNumberRowsSpanned(table, tableCellEdges,
						r, c, tableCell);
				if (rowSpan > 1)
					tableCellElement.setAttribute("rowspan",
							String.valueOf(rowSpan));

				processParagraphes(hwpfDocument, tableCellElement, tableCell,
						table.getTableLevel());

				if (!tableCellElement.hasChildNodes()) {
					tableCellElement.appendChild(htmlDocument.createP());
				}
				tableRowElement.appendChild(tableCellElement);
			}

			if (tableRow.isTableHeader()) {
				tableHeader.appendChild(tableRowElement);
			} else {
				tableBody.appendChild(tableRowElement);
			}
		}

		final Element tableElement = htmlDocument.createTable();
		if (tableHeader.hasChildNodes()) {
			tableElement.appendChild(tableHeader);
		}
		if (tableBody.hasChildNodes()) {
			tableElement.appendChild(tableBody);
			flow.appendChild(tableElement);
		} else {
			logger.log(POILogger.WARN, "Table without body starting at [",
					Integer.valueOf(table.getStartOffset()), "; ",
					Integer.valueOf(table.getEndOffset()), ")");
		}
	}

	protected void processNoteAutonumbered(HWPFDocument doc, String type,
			int noteIndex, Element block, Range noteTextRange) {
		// word目录，暂不处理
	}
}
