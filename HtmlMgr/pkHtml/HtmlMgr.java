/*
 * version: 2010.09.15
 * HtmlMgr.java - HTML Manager of StructureFragmentIdentifier files.
 * :tabSize=2:indentSize=2:noTabs=false:
 * :folding=none:collapseFolds=1:
 *
 * Copyright (C) 1999, 2010 Slava Pestov, Nicholas O'Leary, Dirk Moebius
 * Kaseluris-Nikos (HoKoNoUmo).
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package pkHtml;

//import pk_Util.Util; //iii

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFileChooser;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;
import javax.accessibility.AccessibleText;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.tree.*;
import javax.swing.undo.*;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

/**
 *
 * @modified 2010.06.29
 * @since 2010.04.25 (v00.02.02)
 * @author HoKoNoUmo
 */
public class HtmlMgr extends JPanel//JFrame
{
	static final long serialVersionUID= 21L;
	private String sVersion= "2010.09.15-00.02.03-alpha";//iii, javaws, ppp, nnn

//****************************************************************

	public static DefaultMutableTreeNode cTocNdRoot;

	/** The pair url-title, to display in toc-node. */
	public static Hashtable<String,String>		phUrlTitle=
									new Hashtable<String,String>();
	/** The pair url-tocnode, in order to select a node in toc. */
	public static Hashtable<String,DefaultMutableTreeNode>	phUrlTrNode;
	/** The pair word-TreeSetOfUrls-that-contain it. */
	public static Hashtable<String,TreeSet<String>>	phWord_UrlTS=
									new Hashtable<String,TreeSet<String>>();
	public static String 	sDirHome;

	public static Vector<String> tvWordIgnored= new Vector<String>();

	public HtmlJEditorPaneStructure jEdPnStructure;

//****************************************************************

	private static Icon						iconH0;
	private static Icon						iconH1;
	private static Icon						iconH2;
	private static Icon						iconH3;
	private static Icon						iconH4;
	private static Icon						iconH5;
	private static Icon						iconH6;
	private static Icon						iconH7;
	private static Icon						iconH8;
	private static Icon						iconH9;
	private static Icon						iconH;
	private static Icon						iconParagraph;
	private static Icon 						iconDir;
	private static Icon 						iconDirOpen;
	private static Icon 						iconCheck;
	private static Icon 						iconNoCheck;
	private static HtmlPropertyManager	propMgr;

//****************************************************************

	/** if the right-tabs contain the same information. */
	private boolean									bModified1= false; //editor-ww
	private boolean									bModified2= false;

	private ListenerDocument					lsnDoc;
	private ListenerKey							lsnKeyHndler;
	private ListenerActionURLButton		lsnActUBHistory;
	private ListenerActionURLButton		lsnActUBBookmark;
	private ListenerPropertyEdWw			lsnPropEdWw;
	private ListenerUndoableEdit			lsnUndo;

	private DefaultMutableTreeNode 		cSrchNdRoot;
	private DefaultTreeModel 				cSrchTrMdl;
	private DefaultTreeModel 				cTocTrMdl;

	private int 											niOccurance= 0;

	private HTMLDocument							cEdWwDoc;
	private HtmlEditorKitEditorWW			cEdWwKit;
	private HtmlHistoryBackForward 		historyBF;

	private HtmlMgrAction 		actEUndo;					//Edit
	private HtmlMgrAction 		actERedo;					//Edit
	private HtmlMgrAction 		actECut;						//Edit
	private HtmlMgrAction 		actECopy;					//Edit
	private HtmlMgrAction 		actEPaste;					//Edit
	private HtmlMgrAction 		actEInsHeading;		//Edit
	private HtmlMgrAction 		actEInsParagraph;	//Edit
	private HtmlMgrAction 		actEInsBreak;			//Edit
	private HtmlMgrAction 		actEInsHyperlink;	//Edit
	private HtmlMgrAction 		actEDelElement;		//Edit
	private HtmlMgrAction 		actEFrmBold;				//Edit
	private HtmlMgrAction 		actEFrmItalic;			//Edit
	private HtmlMgrAction 		actEFrmUnderline;	//Edit
	private HtmlMgrAction 		actEUpdateSFI;			//Edit

	private HtmlMgrAction 		actFExit;			//File
	private HtmlMgrAction 		actFNew;				//File
	private HtmlMgrAction 		actFOpenDir;		//File
	private HtmlMgrAction 		actFOpenFile;	//File
	private HtmlMgrAction 		actFOpenUrl;		//File
	private HtmlMgrAction 		actFSave;			//File
	private HtmlMgrAction 		actFSaveAs;		//File

	private HtmlMgrAction 		actGBack;			//GoTo
	private HtmlMgrAction 		actGForward;		//GoTo
	private HtmlMgrAction 		actGHome;			//GoTo
	private HtmlMgrAction 		actGReload;		//GoTo
	private HtmlMgrAction 		actGFind;			//GoTo
	private HtmlMgrAction 		actGListDir;		//GoTo

	private HtmlMgrAction 		actHAbout;			//Help

	private HtmlMgrAction		actVJEPStructure;	//View
	private HtmlMgrAction 		actVWrap;					//View

	private JEditorPane 			cBrsr; //Browser
	private JEditorPane 			cEdWw; //WysiWyg-Editor
	private JFrame						cJFrHtmlMgr;
	private JLabel 					cJLbStatus;
	private JLabel 					cJLbStatus2;	//on StatusBar
	private JMenu						cJMnGoto;
	private JProgressBar			cJPBarBrsr;
	private JProgressBar			cJPBarEdWw;
	private JProgressBar			cJPBarEdSrc;
	private JScrollPane 			cJScrPnBrsr;
	private JScrollPane 			cJScrPnEdWw;
	private JScrollPane 			cJScrPnEdSrc;
	private JScrollPane 			cTocJScrPn;
	private JSplitPane 			cJSplPn;
	private JTabbedPane			cJTbPnL;	//contains the LEFT tabs
	private JTabbedPane			cJTbPnR;	//contains the RIGHT tabs
	private JTextArea				cEdSrc; //tab-Html-Editor
	private JTextField				cJTxFdAddress;
	private JTextField 			cSrchJTxFd;
	private JTree 						cSrchJTr;
	private JTree 						cTocJTr;

	private String						sWhatToDisplay="";
	/** The string-url of the file DISPLAYED. */
	private String						sUrlFile="";
	/** The string-url DISPLAYED = address-bar. */
	private String						sUrlDisplayed="";

	private Vector<String>		lvFilesFound;
	/** Contains the strings of the url of the files in ToC. */
	private Vector<String>		lvUrlFilesToc= new Vector<String>();
	private TreeSet<String>	ltsUrlFilesVisited= new TreeSet<String>();

	private UndoManager			undoMgr= new UndoManager();


	/**
	 * THE START.
	 *
	 * @modified 2010.08.27
	 * @since 2010.04.29 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public static void main(String args[])
	{
		if (args.length== 0)
			new HtmlMgr();
		else if (args.length== 1)
			new HtmlMgr(args[0]);
		else {
			System.out.println("USAGE:");
			System.out.println("java pkHtml.HtmlMgr");
			System.out.println("or");
			System.out.println("java pkHtml.HtmlMgr <path of whatToDisplay>");
		}
	}


	/**
	 * Creates a new user-interface.
	 */
	public HtmlMgr()
	{
		propMgr = new HtmlPropertyManager();
		try {
			propMgr.loadSystemProps(HtmlMgr.class.getResourceAsStream(
				"propertyHtmlMgr.props"));
		} catch(Exception e) {
			System.out.println("Error in loading propertyHtmlMgr.props, "+e.toString());
			System.exit(1);
		}
		sDirHome= System.getProperty("user.dir") + File.separator; //iii
//		sDirHome= Util.AAj_sDir;// nnn ppp
		iconH0 = loadIcon("16x16/hm_h0.png");
		iconH1 = loadIcon("16x16/hm_h1.png");
		iconH2 = loadIcon("16x16/hm_h2.png");
		iconH3 = loadIcon("16x16/hm_h3.png");
		iconH4 = loadIcon("16x16/hm_h4.png");
		iconH5 = loadIcon("16x16/hm_h5.png");
		iconH6 = loadIcon("16x16/hm_h6.png");
		iconH7 = loadIcon("16x16/hm_h7.png");
		iconH8 = loadIcon("16x16/hm_h8.png");
		iconH9 = loadIcon("16x16/hm_h9.png");
		iconH = loadIcon("16x16/hm_h.png");
		iconParagraph = loadIcon("16x16/hm_paragraph.png");
		iconDir = loadIcon("16x16/hm_Dir.png");
		iconDirOpen = loadIcon("16x16/hm_DirOpen.png");
		iconCheck = loadIcon("16x16/hm_check.png");
		iconNoCheck = loadIcon("16x16/hm_MenuCheckNo.png");

		this.lsnActUBHistory = new ListenerActionURLButton(false);
		this.lsnActUBBookmark = new ListenerActionURLButton(true);

		cJFrHtmlMgr= new JFrame();
		cJFrHtmlMgr.setTitle(getProperty("hm.name"));
		historyBF = new HtmlHistoryBackForward();
		createActions();

		JMenuBar jmnBar = createMenu();
		JToolBar jtlBar = createToolbar();
		JPanel jpnAddressBar = createAddressBar();
		jpnAddressBar.addKeyListener(lsnKeyHndler);
		JPanel jpnStatusBar = createStatusBar();

		cJTbPnL = new JTabbedPane();
		//TabToC panel
		JPanel tbTocJPnl= new JPanel(new BorderLayout());
		cTocJTr = new ClassTOCTree();
		cTocJTr.setCellRenderer(new ClassTOCCellRenderer());
		cTocJTr.setEditable(false);
		cTocJTr.setShowsRootHandles(true);
		cTocJScrPn = new JScrollPane(cTocJTr);
//		cTocJScrPn.setAlignmentY(Component.TOP_ALIGNMENT);
//		cTocJScrPn.setAlignmentX(Component.LEFT_ALIGNMENT);
		tbTocJPnl.add(BorderLayout.CENTER,cTocJScrPn);
		cJTbPnL.addTab("tabToc", tbTocJPnl);

		//TabSearch
		JPanel cJPnlSearch= new JPanel(new BorderLayout(6,6));
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(new JLabel("Search for:"));
		box.add(Box.createHorizontalStrut(6));
		box.add(cSrchJTxFd = new JTextField());
		cSrchJTxFd.addActionListener(new ListenerActionSearch());
		cJPnlSearch.add(BorderLayout.NORTH, box);
		cSrchJTr = new JTree();
		cSrchTrMdl = new DefaultTreeModel(cSrchNdRoot);
		cSrchTrMdl.reload(cSrchNdRoot);
		cSrchJTr.setModel(cSrchTrMdl);
		cSrchJTr.setRootVisible(true);
		cSrchJTr.setEditable(false);
		cSrchJTr.setShowsRootHandles(true);
		cSrchJTr.addMouseListener(new ListenerMouseSearch());
		cJPnlSearch.add(BorderLayout.CENTER,
											new JScrollPane(cSrchJTr));
		cJTbPnL.addTab("tabSearch", cJPnlSearch);
		cJTbPnL.setMinimumSize(new Dimension(0,0));

		//the RIGHT tabbedPane
		cJTbPnR = new JTabbedPane();
		cJTbPnR.addChangeListener(new ListenerChangeTabR());

		//the BROWSER component
		cBrsr = new JEditorPane();
		cBrsr.setEditable(false);
		cBrsr.setContentType("text/html;charset=UTF-8");//
		cBrsr.addMouseListener(new ListenerMouseLocator());
		cBrsr.addHyperlinkListener(new ListenerHyperlink());
		cBrsr.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		cBrsr.setFont(new Font("Times New Roman",Font.PLAIN,14));
		cBrsr.addKeyListener(lsnKeyHndler);
		cBrsr.setEditorKit(new HtmlEditorKitBrowser());
//		cBrsr.setMaximumSize(new Dimension(1100, 1100));
		cJScrPnBrsr = new JScrollPane(cBrsr);
		cJTbPnR.addTab(getProperty("hm.browser"),
				loadIcon("16x16/hm_browser.png"), cJScrPnBrsr,
				getProperty("hm.browser.tip"));

		cEdWw= new JEditorPane();
		cEdWw.setEditable(true);
		cEdWw.setContentType("text/html;charset=UTF-8");//
		cEdWw.addMouseListener(new ListenerMouseLocator());
		cEdWw.addHyperlinkListener(new ListenerHyperlink());
		cEdWw.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		cEdWw.setFont(new Font("Times New Roman",Font.PLAIN,14));
		cEdWw.addKeyListener(lsnKeyHndler);
		cEdWw.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"cEdWwEnter");
		cEdWw.getActionMap().put("cEdWwEnter", actEInsParagraph);
//		cEdWw.setMaximumSize(new Dimension(1100, 1100));
		cEdWwKit= new HtmlEditorKitEditorWW();
		cEdWw.setEditorKit(cEdWwKit);
		lsnDoc= new ListenerDocument();
		lsnPropEdWw= new ListenerPropertyEdWw();
		cEdWwDoc= (HTMLDocument)cEdWw.getDocument();
		cEdWwDoc.addDocumentListener(lsnDoc);
		lsnUndo= new ListenerUndoableEdit();
		cEdWwDoc.addUndoableEditListener(lsnUndo);
		cEdWwDoc.putProperty("name", "EditorWW");
		cJScrPnEdWw = new JScrollPane(cEdWw);
		cJTbPnR.addTab(getProperty("hm.editor.ww"),
				loadIcon("16x16/hm_edit-ww.png"), cJScrPnEdWw,
				getProperty("hm.editor.ww.tip"));

		//the Html-Editor
		cEdSrc= new JTextArea();
		cEdSrc.setFont(new Font("Monospaced",Font.PLAIN,16));
		cEdSrc.getDocument().addDocumentListener(lsnDoc);
		cEdSrc.getDocument().addUndoableEditListener(lsnUndo);
		cEdSrc.getDocument().putProperty("name", "EditorSource");
		cEdSrc.addMouseListener(new ListenerMouseLocator());
		cEdSrc.setLineWrap(getPropertyBoolean("hm.actVWrap.selected"));
//		cEdSrc.setMaximumSize(new Dimension(1100, 1100));
//		cEdSrc.setWrapStyleWord(true);
		cJScrPnEdSrc = new JScrollPane(cEdSrc);
		cJTbPnR.addTab(getProperty("hm.editor.source"),
				loadIcon("16x16/hm_html.png"), cJScrPnEdSrc,
				getProperty("hm.editor.source.tip"));

		cJSplPn = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					true,//hhh getBooleanProperty("appearance.continuousLayout"),
					cJTbPnL,
					cJTbPnR);
		cJSplPn.setBorder(null);

		//address, Spliter, status
		JPanel jpnInner = new JPanel(new BorderLayout());
		jpnInner.add(jpnAddressBar, BorderLayout.NORTH);
		jpnInner.add(cJSplPn, BorderLayout.CENTER);
		jpnInner.add(jpnStatusBar, BorderLayout.SOUTH);

		//toolbar, inner-panel
		JPanel jpnOuter = new JPanel(new BorderLayout());
		jpnOuter.add(jtlBar, BorderLayout.NORTH);
		jpnOuter.add(jpnInner, BorderLayout.CENTER);

		//menu, outer-panel
		cJFrHtmlMgr.getContentPane().add(BorderLayout.NORTH,jmnBar);
		cJFrHtmlMgr.getContentPane().add(BorderLayout.CENTER,jpnOuter);

		cJFrHtmlMgr.setPreferredSize(new Dimension(1000,550));
		cJFrHtmlMgr.pack();
		cJFrHtmlMgr.setVisible(true);
		cJFrHtmlMgr.addWindowListener(new ActionFrameClose());
		cJFrHtmlMgr.setLocation(44,5);
//		cJFrHtmlMgr.setExtendedState(JFrame.MAXIMIZED_BOTH);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				cJSplPn.setDividerLocation(259);
				cBrsr.requestFocus();
			}
		});

//put here the option a) display the file from args, b) choose file/dir
//createTocDir(sDirHome+"doc/");//
//createTocXml("file:g:/file1/aajworking/toc.xml");
//createTocFile(HtmlUtilities.createUrlString(sDirHome+"doc/index.html"));//iii
		if (sWhatToDisplay.equals("")){
			File currentDir = new File(System.getProperty("user.dir"));
			File previousDir = currentDir.getParentFile();
			String sDirPrev = previousDir.getAbsolutePath();
			createTocXml("file:" + sDirPrev +"/toc.xml");
//			createTocFile(HtmlUtilities.createUrlString(sDirPrev+"/index.html"));//iii
//			displayUrl("https://htmlmgr.sourceforge.net/index.html", true);
		}
		else {
			if (sWhatToDisplay.startsWith("https:"))
				createTocFile(sWhatToDisplay);
			else {
				File fileWhat= new File(sWhatToDisplay);
				if (fileWhat.exists()){
					if (fileWhat.isDirectory())
						createTocDir("file:"+sWhatToDisplay);
					else
						createTocFile("file:"+sWhatToDisplay);
				}
			}
		}

//		writeIndex();
	}


	/**
	 *
	 * @modified 2010.08.27
	 * @since 2010.08.27 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public HtmlMgr(String sWhatToDisplay) {
		this.sWhatToDisplay= sWhatToDisplay;
		new HtmlMgr();
	}


	/**
	 * Adds on the SearchTree the nodes with the SFIs that contain
	 * a SearchToken (term or word).
	 *
	 * @param sSearch
	 * 		The SearchToken whose SFIs we want to add on the tree.
	 * @param intTkns
	 * 		The number of SearchTokens. The program must know IF there are
	 * 		more than one or not.
	 * @modified 2010.06.01
	 * @since 2010.06.01 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void addTrNodesOnSearchTree(String sSearch, int intTkns){
		String sFileLast="";
		String sFileNew="";
		DefaultMutableTreeNode trndToken;
		DefaultMutableTreeNode trndFile;
		DefaultMutableTreeNode trndOccur;

		//1. get the treeset of urls of the SearchToken
		TreeSet<String> ltsLocations= phWord_UrlTS.get(sSearch);
		if(ltsLocations == null)
			return;
		TreeSet<String> ltsUrls= new TreeSet<String>(ltsLocations);

		//2. get the FIRST-url and create a file-node.
		String sU= ltsUrls.pollFirst();
		sFileLast= getFilePathFromUrlString(sU);
		if (!lvFilesFound.contains(sFileLast))
			lvFilesFound.add(sFileLast);
		niOccurance++;
		trndFile = new DefaultMutableTreeNode(
			HtmlUtilities.setLastPartFirst(sFileLast),true);

		//One token, then add file-node on root-node
		//Many tokens, then add file-node on term-node
		trndToken = new DefaultMutableTreeNode(sSearch, true);
		if (intTkns== 1)
			cSrchNdRoot.add(trndFile);
		else {
			cSrchNdRoot.add(trndToken);
			trndToken.add(trndFile);
		}
		trndOccur = new DefaultMutableTreeNode(sU.substring(sU.indexOf("#")));//?+1?
		trndFile.add(trndOccur);

		//3. put on tree the OTHER-urls.
		for (String sUrl : ltsUrls){
			sFileNew= getFilePathFromUrlString(sUrl);

			if(sFileNew.equals(sFileLast)){
				niOccurance++;
			} else { //search-token on new file
				sFileLast= sFileNew;
				if (!lvFilesFound.contains(sFileNew))
					lvFilesFound.add(sFileNew);
				niOccurance++;
				trndFile = new DefaultMutableTreeNode(
					HtmlUtilities.setLastPartFirst(sFileNew),true);
				//the file-node is put on root or token-node.
				if (intTkns== 1)
					cSrchNdRoot.add(trndFile);
				else {
					trndToken.add(trndFile);
				}
			}

			trndOccur = new DefaultMutableTreeNode(sUrl.substring(sUrl.indexOf("#")));//?+1?
			trndFile.add(trndOccur);
		}
	}


	/**
	 * Adds the html|txt-files of the specified-directory, recursively,
	 * as children to the tree-node in the ToC-tree
	 *
	 * @modified 2010.04.27
	 * @since 2010.04.27 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void addTrNodesRecursively(
				DefaultMutableTreeNode rtNode,
				File dir,
				FilenameFilter filter)
	{
		File[] entries = dir.listFiles();
		DefaultMutableTreeNode node;
		//first add the files
		for (File entry : entries) {
			if (entry.isFile() && filter.accept(dir, entry.getName())) {
				String filename= entry.getAbsoluteFile().toString();

				//IF, it is an html, parse it and make toc of h1,...h6
				if (filename.endsWith("html")||filename.endsWith("htm")){
					//1. Create treemap <position, url>
					//2. create hashtable <url, title>
					//3. create hashtable <word, url>
					String sUrl= HtmlUtilities.createUrlString(filename);
					//CHECK if it is SFI-Html
					if (HtmlUtilities.isSFIFile(sUrl)) {
						lvUrlFilesToc.add(sUrl);
						ltsUrlFilesVisited.add(sUrl);
						new HtmlParser(sUrl, null, rtNode);
					}
					else {
						JOptionPane.showMessageDialog(null,
							"HtmlParser: NOT SFI-file: "+sUrl);
						openBrowser(sUrl);
						break;
					}
				}
			}
		}
		//second, add directories
		for (File entry : entries) {
			if (entry.isDirectory()){
				node= createTocNode("dir::"+entry.getAbsoluteFile().toString());
				rtNode.add(node);
				addTrNodesRecursively(node, entry, filter);
			}
		}
	}


	/**
	 * Create the actions of the menu.
	 */
	private void createActions()
	{
		actFExit = new ActionFileExit();
		actFNew = new ActionFileNew();
		actFOpenFile = new ActionFileOpenFile();
		actFOpenUrl = new ActionFileOpenUrl();
		actFOpenDir = new ActionFileOpenDir();
		actFSave = new ActionFileSave();
		actFSaveAs = new ActionFileSaveAs();

		//EDIT-ACTIONS
		actEUndo= new ActionEditUndo();
		actERedo= new ActionEditRedo();
		actECut= new ActionEditCut();
		actECopy= new ActionEditCopy();
		actEPaste= new ActionEditPaste();
		actEInsHeading= new ActionEditInsertHeading();
		actEInsParagraph= new ActionEditInsertParagraph();
		actEInsBreak= new ActionEditInsertBreak();
		actEInsHyperlink= new ActionEditInsertHyperlink();
		actEDelElement= new ActionEditDeleteElement();
		actEUpdateSFI= new ActionEditUpdateSFI();
		actEFrmBold= new ActionEditFormatBold();
		actEFrmItalic= new ActionEditFormatItalic();
		actEFrmUnderline= new ActionEditFormatUnderline();

		actGBack = new ActionGotoBack();
		actGForward = new ActionGotoForward();
		actGHome = new ActionGotoHome();
		actGReload = new ActionGotoReload();
		actGFind = new ActionGotoFind();
		actGListDir = new ActionGotoListDirectory();

		actHAbout = new ActionHelpAbout();

		actVJEPStructure = new ActionViewHtmlJEditorPaneStructure();
		actVWrap = new ActionViewWrap();
//		actOpenBuffer = new open_buffer();
//		actEditURL = new edit_url();
//		actCopy = new copy();
//		actSelectAll = new select_all();
//		actOpenLocation = new OpenLocation();
//		actBookmarksAdd = new bookmarks_add();
//		actBookmarksEdit = new bookmarks_edit();
//		actFollowLink = new follow_link();
	}


	/**
	 *
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author Dirk Moebius
	 */
	private JPanel createAddressBar()
	{
		// the url textfield
		cJTxFdAddress = new JTextField();
		cJTxFdAddress.setFocusAccelerator('l');
		cJTxFdAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				displayUrl(cJTxFdAddress.getText(), true);
			}
		});

		// url textfield and label
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(
							getProperty("hm.label.address")), BorderLayout.WEST);
		panel.add(cJTxFdAddress, BorderLayout.CENTER);

		return panel;
	}


	/**
	 *
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private JMenuBar createMenu()
	{
		// File menu
		JMenu cJMnFile = new JMenu(getProperty("hm.menu.file"));
		cJMnFile.setMnemonic(getProperty("hm.menu.file.mnemonic").charAt(0));
		cJMnFile.setFont(new Font("Dialog", Font.PLAIN, 16));
		cJMnFile.add(actFNew);
		cJMnFile.add(actFOpenFile);
		cJMnFile.add(actFOpenUrl);
		cJMnFile.add(actFOpenDir);
		cJMnFile.add(new JSeparator());
		cJMnFile.add(actFSave);
		cJMnFile.add(actFSaveAs);
		cJMnFile.add(new JSeparator());
		cJMnFile.add(actFExit);
//		cJMnFile.add(aOpenBuffer);
//		cJMnFile.add(aEditURL);

		// Edit menu
		JMenu cJMnEdit = new JMenu(getProperty("hm.menu.edit"));
		cJMnEdit.setMnemonic(getProperty("hm.menu.edit.mnemonic").charAt(0));
		cJMnEdit.setFont(new Font("Dialog", Font.PLAIN, 16));
		cJMnEdit.add(actEUndo);
		cJMnEdit.add(actERedo);
		cJMnEdit.add(new JSeparator());
		cJMnEdit.add(actECut);
		cJMnEdit.add(actECopy);
		cJMnEdit.add(actEPaste);
		cJMnEdit.add(new JSeparator());
		cJMnEdit.add(actEInsHeading);
		cJMnEdit.add(actEInsParagraph);
		cJMnEdit.add(actEInsBreak);
		cJMnEdit.add(actEInsHyperlink);
		cJMnEdit.add(new JSeparator());
		cJMnEdit.add(actEDelElement);
		cJMnEdit.add(new JSeparator());
		cJMnEdit.add(actEFrmBold);
		cJMnEdit.add(actEFrmItalic);
		cJMnEdit.add(actEFrmUnderline);
		cJMnEdit.add(new JSeparator());
		cJMnEdit.add(actEUpdateSFI);
//		mEdit.add(aCopy);
//		mEdit.add(aSelectAll);

		// View menu
		JMenu cJMnView = new JMenu(getProperty("hm.menu.view"));
		cJMnView.setMnemonic(getProperty("hm.menu.view.mnemonic").charAt(0));
		cJMnView.setFont(new Font("Dialog", Font.PLAIN, 16));
		cJMnView.setToolTipText(getProperty("hm.menu.view.tip"));
		JMenuItem item = actVWrap.menuItem();
		cJMnView.add(item);
		cJMnView.add(actVJEPStructure);

		//GoTo menu
		cJMnGoto = new JMenu(getProperty("hm.menu.goto"));
		cJMnGoto.setMnemonic(getProperty("hm.menu.goto.mnemonic").charAt(0));
		cJMnGoto.setFont(new Font("Dialog", Font.PLAIN, 16));
		cJMnGoto.setToolTipText(getProperty("hm.menu.goto.tip"));
		updateGotoMenu();

		// Bookmarks menu
//		mBmarks = new JMenu(getProperty("hm.menu.bookmarks"));
//		mBmarks.setMnemonic(getProperty("hm.menu.bookmarks.mnemonic").charAt(0));
//		updateBookmarksMenu();

		//Format menu

		//Insert menu

		// Help menu
		JMenu jmnHelp = new JMenu(getProperty("hm.menu.help"));
		jmnHelp.setMnemonic(getProperty("hm.menu.help.mnemonic").charAt(0));
		jmnHelp.setFont(new Font("Dialog", Font.PLAIN, 16));
		jmnHelp.add(actHAbout);


		// Menubar
		JMenuBar jmnBar = new JMenuBar();
		jmnBar.add(cJMnFile);
		jmnBar.add(cJMnEdit);
		jmnBar.add(cJMnView);
		jmnBar.add(cJMnGoto);
		jmnBar.add(jmnHelp);
		return jmnBar;
	}


	/**
	 *
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author Dirk Moebius
	 */
	private JPanel createStatusBar()
	{
		// the status text field
		cJLbStatus= new JLabel("Html-Manager");
		cJLbStatus.setBorder(new BevelBorder(BevelBorder.LOWERED));
		cJLbStatus.setFont(new Font("Dialog", Font.PLAIN, 12));
		cJLbStatus.setMinimumSize(new Dimension(200, cJLbStatus.getPreferredSize().height));

		cJPBarBrsr= new JProgressBar(0,100);
		cJPBarBrsr.setMaximumSize(new Dimension(8, cJPBarBrsr.getPreferredSize().height));
		cJPBarBrsr.setBorder(new BevelBorder(BevelBorder.LOWERED));
//		cJPBarBrsr.setStringPainted(true);
//		cJPBarBrsr.setIndeterminate(true);


		cJPBarEdWw= new JProgressBar(0,100);
		cJPBarEdWw.setMaximumSize(new Dimension(8, cJPBarEdWw.getPreferredSize().height));
		cJPBarEdWw.setBorder(new BevelBorder(BevelBorder.LOWERED));
//		cJPBarEdWw.setStringPainted(true);

		cJPBarEdSrc= new JProgressBar(0,100);
		cJPBarEdSrc.setMaximumSize(new Dimension(8, cJPBarEdSrc.getPreferredSize().height));
		cJPBarEdSrc.setBorder(new BevelBorder(BevelBorder.LOWERED));
//		cJPBarEdSrc.setStringPainted(true);

		// the title text field
		cJLbStatus2 = new JLabel("version: "+sVersion);
		cJLbStatus2.setBorder(new BevelBorder(BevelBorder.LOWERED));
		cJLbStatus2.setFont(new Font("Dialog", Font.PLAIN, 12));
		cJLbStatus2.setMaximumSize(new Dimension(20, cJLbStatus2.getPreferredSize().height));

		JPanel jpBar= new JPanel(new GridLayout(1,3));
		jpBar.add(cJPBarBrsr);
		jpBar.add(cJPBarEdWw);
		jpBar.add(cJPBarEdSrc);

		JPanel jpBarVer= new JPanel(new BorderLayout());
		jpBarVer.add(jpBar, BorderLayout.WEST);
		jpBarVer.add(cJLbStatus2, BorderLayout.EAST);


		// status and title field
//		JPanel statusBar = new JPanel(new GridLayout(1, 3));
//		statusBar.add(cJLbStatus);
//		statusBar.add(jpBar);
//		statusBar.add(cJLbStatus2);

		JPanel statusBar = new JPanel(new BorderLayout());
		statusBar.add(cJLbStatus, BorderLayout.CENTER);
		statusBar.add(jpBarVer, BorderLayout.EAST);

		return statusBar;
	}


	/**
	 * Creates the ToC from the files in a directory, recursively.
	 *
	 * @param strDir
	 * 		The Absolute-directory from which we will create the ToC.
	 * @modified 2010.05.17
	 * @since 2010.04.27 (v00.02.02)
	 * @author Slava Pestov
	 */
	public void createTocDir(String strDir)
	{
		initialize();

		cTocNdRoot = createTocNode("dir::"+strDir);//new DefaultMutableTreeNode();
		addTrNodesRecursively(cTocNdRoot, new File(strDir), new HtmlFileFilter());
		cTocTrMdl = new DefaultTreeModel(cTocNdRoot);
		cTocTrMdl.reload(cTocNdRoot);
		cTocJTr.setModel(cTocTrMdl);

		File idxFile = new File(strDir+"index.html");
		File idxFile2 = new File(strDir+"index.htm");
		if (idxFile.exists())
			displayUrl(HtmlUtilities.createUrlString(strDir+"index.html"), false);
		else if (idxFile2.exists())
			displayUrl(HtmlUtilities.createUrlString(strDir+"index.htm"), false);
		else {
			File flDir = new File(strDir);
			File[] entries = flDir.listFiles();
			HtmlFileFilter filter= new HtmlFileFilter();
			for (File entry : entries) {
				if (entry.isFile() && filter.accept(flDir, entry.getName())){
					String filename= entry.getAbsoluteFile().toString();
					sUrlFile= HtmlUtilities.createUrlString(filename);
					displayUrl(sUrlFile, false);
				}
			}
		}
	}


	/**
	 * @param sUrl
	 * 		The url (file:g:/file1...)
	 * @modified 2010.05.17
	 * @since 2010.05.17 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public void createTocFile(String sUrl)
	{
		String sFile= sUrl;
		if (sUrl.indexOf("#")!=-1)
			sFile= getUrlFileOfUrl(sUrl);
		sFile= HtmlUtilities.createUrlString(sFile);
		if (HtmlUtilities.isSFIFile(sFile)){
			initialize();
			sUrlFile= sFile;
			lvUrlFilesToc.add(sUrlFile);
			ltsUrlFilesVisited.add(sUrlFile);
			new HtmlParser(sUrlFile, null, null);
			cTocTrMdl = new DefaultTreeModel(cTocNdRoot);
			cTocTrMdl.reload(cTocNdRoot);
			cTocJTr.setModel(cTocTrMdl);
			cTocJTr.setRootVisible(true);
			displayUrl(sUrl, false);
		}
		else {
			JOptionPane.showMessageDialog(null,"HtmlParser: NOT SFI-file: "+sUrl);
			openBrowser(sUrl);
			return;
		}

	}


	/**
	 * Displays html-data from string. Its url is the one
	 * in the address-bar.
	 *
	 * @modified 2010.08.12
	 * @since 2010.08.12 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public void createTocHtml(String sHtml) {
		//we leave the same info at addressBar.
		initialize();
		String sUrl= getUrlFileOfUrl(cJTxFdAddress.getText());
		lvUrlFilesToc.add(sUrl);
		new HtmlParser(sHtml, sUrl, null);

//		String sAddress= getUrlFileOfUrl(cJTxFdAddress.getText());
//		lvUrlFilesToc.add(sAddress);
//		if (HtmlUtilities.isSFIHtml(sHtml))
//		else {
//			JOptionPane.showMessageDialog(null,
//				"HtmlParser: NOT SFI-Html:");
//			return;
//		}

		cTocTrMdl = new DefaultTreeModel(cTocNdRoot);
		cTocTrMdl.reload(cTocNdRoot);
		cTocJTr.setModel(cTocTrMdl);
		cTocJTr.setRootVisible(true);

		displayHtml(sHtml);
	}


	/**
	 * Creates a tree-node. If it is a directory-node,
	 * puts it in pair "url-node". If not, puts in pair "url-title"
	 * the last part of dir as title, which it displays on tree.
	 *
	 * @param sUrl
	 * 		The url of the location file or doc, the node can show.
	 * @modified 2010.05.16
	 * @since 2010.05.16 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public static DefaultMutableTreeNode createTocNode(String sUrl){
//System.out.println("..createNode: "+ sUrl);
		DefaultMutableTreeNode trNode = new DefaultMutableTreeNode(
													sUrl, true);//allowsChildren if specified
		if (!sUrl.startsWith("dir::"))
			phUrlTrNode.put(sUrl, trNode);
		else
			storeTitle(sUrl, null);

		return trNode;
	}


	/**
	 * Creates a ToC of html-files by reading an xml-file
	 * holding the structure of the ToC.
	 *
	 * @param sUrl
	 * 		The url of the "toc.xml" file.
	 * @modified 2010.05.20
	 * @since 2010.05.20 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public void createTocXml(String sUrl)
	{
		historyBF = new HtmlHistoryBackForward();
		phUrlTitle= new Hashtable<String,String>();
		phUrlTrNode= new Hashtable<String,DefaultMutableTreeNode>();
		phWord_UrlTS= new Hashtable<String,TreeSet<String>>();

//System.out.println(sUrl);
		ClassToCXmlHandler hdlr = new ClassToCXmlHandler();
		try {
			HtmlUtilities.parseXML(new URL(sUrl).openStream(), hdlr);
		}
		catch(IOException e) {
			System.out.println(e.toString());
		}

		cTocTrMdl = new DefaultTreeModel(cTocNdRoot);
		cTocTrMdl.reload(cTocNdRoot);
		cTocJTr.setModel(cTocTrMdl);
		cTocJTr.setRootVisible(true);
//		cTocJTr.makeVisible(new TreePath(cTocNdRoot));

System.out.println("xml.disp: "+hdlr.sUrlDisplay);
		displayUrl(hdlr.sUrlDisplay, false);
	}


	/**
	 *
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private JToolBar createToolbar()
	{
		JToolBar jtBar = new JToolBar(JToolBar.HORIZONTAL);

		jtBar.add(actGBack);
		jtBar.add(actGForward);

		jtBar.addSeparator();
		jtBar.add(actFOpenFile);
		jtBar.add(actFOpenUrl);
		jtBar.add(actFOpenDir);

		jtBar.addSeparator();
		jtBar.add(actGReload);
		jtBar.add(actGHome);

		jtBar.addSeparator();
		jtBar.add(actEUndo);
		jtBar.add(actERedo);

		jtBar.addSeparator();
		jtBar.add(actECut);
		jtBar.add(actECopy);
		jtBar.add(actEPaste);

		jtBar.addSeparator();
		jtBar.add(actEInsHeading);
		jtBar.add(actEInsParagraph);
		jtBar.add(actEInsBreak);
		jtBar.add(actEInsHyperlink);

		jtBar.addSeparator();
		jtBar.add(actEDelElement);

		jtBar.addSeparator();
		jtBar.add(actEFrmBold);
		jtBar.add(actEFrmItalic);
		jtBar.add(actEFrmUnderline);
//		jtBar.add(aEditURL);
//		jtBar.add(aOpenBuffer);
//		jtBar.add(Box.createHorizontalGlue());

		return jtBar;
	}


	/**
	 * Delays the execution of commands, given the milliseconds.
	 *
	 * @modified 2010.08.08
	 * @since 2010.08.08 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public void delay(long nl) {
		try {
			Thread.currentThread().sleep(nl);
		} catch (InterruptedException ie){
			System.out.println(ie.getMessage());
		}
	}


	/**
	 * Displays an html-string in the 3 text-components, goes to
	 * the address in the address-bar and selects the appropriate node
	 * in the ToC.
	 *
	 * @modified 2010.08.12
	 * @since 2010.08.12 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public void displayHtml(String sHtml) {
		if (sHtml.equals("") || sHtml==null)
			return;

		try {
			//set browser
//			cBrsr.setMaximumSize(new Dimension(1100, 1100));
			cBrsr.setText(sHtml);
			//set ww-editor
			setText(cEdWw, sHtml);
			//set src-editor
			setText(	cEdSrc, sHtml);
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		gotoFIinAddress();
		selectTreeNode(cJTxFdAddress.getText());
	}


	/**
	 *
	 * @modified 2010.05.16
	 * @since 2010.05.16 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public void displayUrl(String sUrl)
	{
		displayUrl(sUrl, true);
	}


	/**
	 * Displays the specified URL in the selected HTML-tab.<br/>
	 * - sets the window-title.<br/>
	 * - selects the node in ToC.
	 *
	 * @param sUrl
	 * 		The directory or file we want to display.
	 * @param bAddToHistory
	 * 		Should the URL be added to the back/forward history?
	 */
	public void displayUrl(String sUrl, boolean bAddToHistory)
	{
		//on directories, show nothing
		if (sUrl.startsWith("dir::") || sUrl.equals("") || sUrl==null)
			return;

//		cJLbStatus.setText("... " +sUrl);

		String sFileUrl= getUrlFileOfUrl(sUrl);
		if (!lvUrlFilesToc.contains(sFileUrl)) {
			createTocFile(sUrl);
			return;
		}

		try {
			final URL _url = new URL(sUrl);
			if (!getUrlFileOfUrl(sUrl).equals(getUrlFileOfUrl(sUrlDisplayed))){

				//set browser
				cJPBarBrsr.setIndeterminate(true);
				cBrsr.setPage(_url);
				cBrsr.addPropertyChangeListener( new PropertyChangeListener() {
					public void propertyChange( PropertyChangeEvent e ) {
						if ( e.getPropertyName().equals( "page" ) ) {
							//set source-editor
							cJPBarBrsr.setIndeterminate(false);
							setText(	cEdSrc, getSfiHtml(cBrsr));
						}
					}
				});

				//set ww-editor
				cJPBarEdWw.setIndeterminate(true);
				cEdWw.getDocument().removeDocumentListener(lsnDoc);
				cEdWw.getDocument().removeUndoableEditListener(lsnUndo);
				cEdWw.setPage(_url);
				cEdWw.addPropertyChangeListener("page", lsnPropEdWw);
//				cEdWw.addPropertyChangeListener("page", new PropertyChangeListener() {
//					public void propertyChange( PropertyChangeEvent e ) {
//						cJPBarEdWw.setIndeterminate(false);
//						cEdWw.getDocument().addDocumentListener(lsnDoc);
//						cEdWw.getDocument().addUndoableEditListener(lsnUndo);
//					}
//				});

				if (sUrl.indexOf("#")==-1){
					setWindowTitle(phUrlTitle.get(sUrl));
					if (!sUrlFile.equals(sUrl))
						sUrlFile= sUrl;
				} else { //SFI
					String sUF= sUrl.substring(0, sUrl.indexOf("#"));
					setWindowTitle(phUrlTitle.get(sUF));
					if (!sUrlFile.equals(sUF))
						sUrlFile= sUF;
				}
			}
			cJTxFdAddress.setText(sUrl);
			//only in source
//			if (cJTbPnR.getSelectedIndex()==2)
			gotoFIinAddress();
		}
		catch(MalformedURLException mf) {
			error(this,mf.getMessage());
			return;
		}
		catch(IOException io) {
			error(this,io.toString());
			return;
		}

		if (bAddToHistory && sUrlDisplayed!=null)
			historyBF.add(sUrlDisplayed);
		sUrlDisplayed= sUrl;
		updateBFButtons();
		updateGotoMenu();

		// select the appropriate tree-node.
		selectTreeNode(sUrl);//hhh
		cJLbStatus.setText(sUrlDisplayed);

	}


	/**
	 * Displays an error dialog box.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property. The message
	 * is formatted by the property manager with <code>args</code> as
	 * positional parameters.
	 * @param comp The component to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 */
	public static void error(Component comp, String name)
	{
		JOptionPane.showMessageDialog(comp, name);
	}


	/**
	 * Returns an array of start|end offsets of the first match
	 * of a regular-expression in the selected text-component,
	 * after a given offset.
	 *
	 * @modified 2010.08.11
	 * @since 2010.08.11 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private int[] findRegEx(String sRE, int niOffset)
	{
		int aSE[]= {-1,-1}; //arrayStartEnd
		JTextComponent cJTxToSearch= (JTextComponent)cBrsr;
		if (cJTbPnR.getSelectedIndex()==1)
			cJTxToSearch= (JTextComponent)cEdWw;
		else if (cJTbPnR.getSelectedIndex()==2)
			cJTxToSearch= (JTextComponent)cEdSrc;
		int niSearchPos = -1;
		try {
			AbstractDocument doc = (AbstractDocument)cJTxToSearch.getDocument();
			String sToSearch= doc.getText(niOffset, doc.getLength()-niOffset);

			Pattern pattern = Pattern.compile(sRE);
			Matcher matcher = pattern.matcher(sToSearch);
			boolean bMatchFound = matcher.find();

			if(bMatchFound) {
				aSE[0]= niOffset+matcher.start();
				aSE[1]= niOffset+matcher.end();
			}
		}
		catch(BadLocationException ble) {
			System.out.println("!!!ble: HtmlMgr.findRegEx: "+ble.toString());
		}
		return aSE;
	}


	/**
	 *
	 * @modified 2010.08.19
	 * @since 2010.08.19 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private String findSFIofVisibleElement()
	{
		String sSFI= "";
		int niP= -1;
		String sElTxt="";
		HTMLDocument hDoc= (HTMLDocument)cBrsr.getDocument();
		niP= cBrsr.viewToModel(((JViewport)cBrsr.getParent()).getViewPosition());
		Element elem= hDoc.getParagraphElement(niP);

		if (cJTbPnR.getSelectedIndex()==0){
			sElTxt= getTextOfElement(hDoc, elem);
			if (sElTxt.indexOf("#")!=-1)
				sSFI= sElTxt.substring(sElTxt.indexOf("#")+1,
												sElTxt.indexOf("#",sElTxt.indexOf("#")+1));
		}
		else if (cJTbPnR.getSelectedIndex()==1){
			hDoc= (HTMLDocument)cEdWw.getDocument();
			niP= cEdWw.viewToModel(((JViewport)cEdWw.getParent()).getViewPosition());
			elem= hDoc.getParagraphElement(niP);
			sElTxt= getTextOfElement(hDoc, elem);
			if (sElTxt.indexOf("#")!=-1)
				sSFI= sElTxt.substring(sElTxt.indexOf("#")+1,
												sElTxt.indexOf("#",sElTxt.indexOf("#")+1));
		}
		else if (cJTbPnR.getSelectedIndex()==2){
//			<a name="h0.2.1"></a>
//			<a name="ifiSFI"></a>
			niP= cEdSrc.getCaretPosition();
			PlainDocument docPl= (PlainDocument)cEdSrc.getDocument();
			Element eRoot= docPl.getDefaultRootElement();
			int niIdxCh= eRoot.getElementIndex(niP);
			for (int i=niIdxCh; i>0; i--){
				//finds the PREVIOUS element with SFI
				Element eP= eRoot.getElement(i);
				String sTx= getTextOfElement(docPl,eP);
				if (sTx.indexOf("name=\"h0") != -1){
					sElTxt= sTx;
					break;
				}
			}
			sSFI= sElTxt.substring(sElTxt.indexOf("\"")+1,
												sElTxt.indexOf("\"",sElTxt.indexOf("\"")+1));
		}
		return sSFI;
	}


	/**
	 * Finds the StartOffset of the cursor's-SFI-element.
	 *
	 * @modified 2010.08.21
	 * @since 2010.08.21 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private int findStartOffsetOfCurrentSFIElement()
	{
		int niP= -1;
		HTMLDocument hDoc= (HTMLDocument)cBrsr.getDocument();
		niP= cBrsr.getCaretPosition();
		Element elem= hDoc.getParagraphElement(niP);

		if (cJTbPnR.getSelectedIndex()==0){
			niP= elem.getStartOffset();
		}
		else if (cJTbPnR.getSelectedIndex()==1){
			hDoc= (HTMLDocument)cEdWw.getDocument();
			niP= cEdWw.getCaretPosition();
			elem= hDoc.getParagraphElement(niP);
			niP= elem.getStartOffset();
		}

		else if (cJTbPnR.getSelectedIndex()==2){
//			<a name="h0.2.1"></a>
//			<a name="ifiSFI"></a>
			niP= cEdSrc.getCaretPosition();
			PlainDocument docPl= (PlainDocument)cEdSrc.getDocument();
			Element eRoot= docPl.getDefaultRootElement();
			int niIdxCh= eRoot.getElementIndex(niP);
			for (int i=niIdxCh; i>0; i--){
				//finds the PREVIOUS element with SFI
				Element eP= eRoot.getElement(i);
				String sTx= getTextOfElement(docPl,eP);
				if (sTx.indexOf("name=\"h0") != -1){
					niP= eP.getStartOffset();
					break;
				}
			}
		}
		return niP;
	}


	/**
	 * Finds in the selected-tab's-document some text, from a given-offset.
	 *
	 * @modified 2010.08.08
	 * @since 2010.08.08 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private int findText(String sFindTxt, boolean bCaseSenstive, int niOffset)
	{
		JTextComponent cJTxToSearch= (JTextComponent)cBrsr;
		if (cJTbPnR.getSelectedIndex()==1)
			cJTxToSearch= (JTextComponent)cEdWw;
		else if (cJTbPnR.getSelectedIndex()==2)
			cJTxToSearch= (JTextComponent)cEdSrc;
		int niSearchPos = -1;
		try {
			AbstractDocument doc = (AbstractDocument)cJTxToSearch.getDocument();
			niSearchPos =
				(bCaseSenstive ?
					doc.getText(0, doc.getLength()).indexOf(sFindTxt, niOffset) :
					doc.getText(0, doc.getLength()).toLowerCase().indexOf(sFindTxt.toLowerCase(), niOffset)
				);
			if(niSearchPos > -1) {
				cJTxToSearch.requestFocus();

				JScrollBar jsBar = cJScrPnEdSrc.getVerticalScrollBar();
				if (cJTbPnR.getSelectedIndex()==0)
					jsBar = cJScrPnBrsr.getVerticalScrollBar();
				else if (cJTbPnR.getSelectedIndex()==1)
					jsBar = cJScrPnEdWw.getVerticalScrollBar();

				if (sFindTxt.startsWith("h0.") && sFindTxt.indexOf("p")==-1) {
					//selected-text is ALWAYS at the TOP of the screen.
					jsBar.setValue(doc.getLength());
				}
				else {
					//selected-text is ALWAYS at the BOTTOM of the screen.
					jsBar.setValue(0);
				}
				cJTxToSearch.select(niSearchPos, niSearchPos + sFindTxt.length());
			}
		}
		catch(BadLocationException ble) {
			System.out.println("!!!ble: HtmlMgr.findText: "+ble.toString());
		}
		return niSearchPos;
	}


	public static String getDirHome()
	{
		return sDirHome;
	}


	/**
	 * Returns the last-part of the specified path.
	 *
	 * @param path The path name
	 */
	public static String getFileName(String path)
	{
		if(path.equals("/"))
			return path;

		while(path.endsWith("/") || path.endsWith(File.separator))
			path = path.substring(0,path.length() - 1);

		int index = Math.max(path.lastIndexOf('/'),
			path.lastIndexOf(File.separatorChar));
		if(index == -1)
			index = path.indexOf(':');

		// don't want getFileName("roots:") to return ""
		if(index == -1 || index == path.length() - 1)
			return path;

		return path.substring(index + 1);
	}


	/**
	 * INPUT: file:g:/file1/htmlmgr/doc/index.html#ifiSFI <br/>
	 * OUTPUT: g:/file1/htmlmgr/doc/index.html
	 *
	 * @modified 2010.08.13
	 * @since 2010.08.13 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public String getFilePathFromUrlString(String sUrl){
		String sPath= "";

		if (sUrl.startsWith("http")) {
			sPath= sUrl;
			if (sPath.indexOf("#")!=-1)
				sPath= sPath.substring(0,sPath.indexOf("#"));
			return sPath;
		}
		else if (sUrl.startsWith("file")) {
			sPath= sUrl.substring(5); //wrka
			if (sPath.indexOf("#")!=-1)
				sPath= sPath.substring(0,sPath.indexOf("#"));
		}

		return sPath;
	}


	/**
	 * Returns the parent of the specified path.
	 *
	 * @param path The path name
	 * @since jEdit 2.6pre5
	 * @author Slava Pestov
	 */
	public static String getParentOfPath(String path)
	{
		// ignore last character of path to properly handle
		// paths like /foo/bar/
		int lastIndex = path.length() - 1;
		while(lastIndex > 0
			&& (path.charAt(lastIndex) == File.separatorChar
			|| path.charAt(lastIndex) == '/'))
		{
			lastIndex--;
		}

		int count = Math.max(0,lastIndex);
		int index = path.lastIndexOf(File.separatorChar,count);
		if(index == -1)
			index = path.lastIndexOf('/',count);
		if(index == -1)
		{
			// this ensures that getFileParent("protocol:"), for
			// example, is "protocol:" and not "".
			index = path.lastIndexOf(':');
		}

		return path.substring(0,index + 1);
	}


	/**
	 * Fetches a property, returning null if it's not defined.
	 * @param name The property
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author Slava Pestov
	 */
	public static String getProperty(String name)
	{
		return propMgr.getProperty(name);
	}


	/**
	 * Returns the property with the specified name.<p>
	 *
	 * The elements of the <code>args</code> array are substituted
	 * into the value of the property in place of strings of the
	 * form <code>{<i>n</i>}</code>, where <code><i>n</i></code> is an index
	 * in the array.<p>
	 *
	 * You can find out more about this feature by reading the
	 * documentation for the <code>format</code> method of the
	 * <code>java.text.MessageFormat</code> class.
	 *
	 * @param name The property
	 * @param args The positional parameters
	 * @author Slava Pestov
	 */
	public static String getProperty(String name, Object[] args)
	{
		if(name == null)
			return null;
		if(args == null)
			return getProperty(name);
		else {
			String value = getProperty(name);
			if(value == null)
				return null;
			else
				return MessageFormat.format(value,args);
		}
	}


	/**
	 * Returns the value of a boolean property.
	 * @param name The property
	 */
	public static boolean getPropertyBoolean(String name)
	{
		return getPropertyBoolean(name,false);
	}


	/**
	 * Returns the value of a boolean property.
	 * @param name The property
	 * @param def The default value
	 */
	public static boolean getPropertyBoolean(String name, boolean def)
	{
		String value = getProperty(name);
		return HtmlUtilities.getBoolean(value, def);
	}


	/**
	 * From a JEditorPane retrieves the Html code
	 * as a StructureFragmentIdentifier-file.
	 *
	 * @modified 2010.08.05
	 * @since 2010.08.05 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private String getSfiHtml(JEditorPane jep){
		StringWriter wrStr= new StringWriter();
		HTMLDocument hDoc= (HTMLDocument)jep.getDocument();
		HtmlEditorKitEditorWW htmlKit= (HtmlEditorKitEditorWW)cEdWw.getEditorKit();
		try {
			htmlKit.write(wrStr, hDoc, 0, hDoc.getLength());
			wrStr.flush();
			wrStr.close();
		} catch (Exception ew) {
			System.out.println(ew.toString());
		}
		return wrStr.toString();
	}


	/**
	 *
	 * @modified 2010.07.10
	 * @since 2010.07.10 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public String getTextOfElement(Document doc, Element elem)
	{
		String sTxt="";
		int startOffset = elem.getStartOffset();
		int endOffset = elem.getEndOffset();
		int length = endOffset - startOffset;
		try {
			sTxt= doc.getText(startOffset, length);
		} catch (BadLocationException ble) {
			System.out.println(ble.toString());
		}
		return sTxt;
	}


	/**
	 * INPUT: file:g:/file1/htmlmgr/doc/index.html#ifiSFI <br/>
	 * OUTPUT: file:g:/file1/htmlmgr/doc/index.html
	 *
	 * @modified 2010.07.11
	 * @since 2010.07.11 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public String getUrlFileOfUrl(String sUrl)
	{
		if (sUrl.indexOf("#")==-1)
			return sUrl;
		else
			return sUrl.substring(0, sUrl.indexOf("#"));
	}


	/**
	 * Go back in history. Beep, if that's not possible.
	 *
	 * @modified 2010.04.30
	 * @since 2010.04.30 (v00.02.02)
	 * @author Dirk Moebius
	 */
	public void goBack()
	{
		String sUrlPrev = historyBF.getPrevious(sUrlDisplayed);

		if (sUrlPrev == null) {
			getToolkit().beep();
		}
		else {
			displayUrl(sUrlPrev, false);
		}
	}


	/**
	 * Go forward in history. Beep if that's not possible.
	 *
	 * @modified 2010.04.30
	 * @since 2010.04.30 (v00.02.02)
	 * @author Dirk Moebius
	 */
	public void goForward()
	{
		String sUrlNext = historyBF.getNext(sUrlDisplayed);

		if (sUrlNext == null)
			getToolkit().beep();
		else
			displayUrl(sUrlNext, false);
	}


	/**
	 * Lists the contents of a directory.
	 *
	 * @modified 2010.05.02
	 * @since 2010.05.02 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public void listDirectory(String stDir)
	{

		if (!stDir.startsWith("file:"))
			stDir= "file:"+stDir;
		cJTxFdAddress.setText(stDir);

		try {
			URL _url = new URL(stDir);
			cEdWw.setPage(_url);
		} catch(MalformedURLException mf) {
			error(this,mf.getMessage());
			return;
		}
		catch(IOException io)
		{
			error(this,io.toString());
			return;
		}
		cEdWw.requestFocus();
	}


	/**
	 * Goes, in the selected tab, to the #FI in the address-bar.
	 *
	 * @modified 2010.08.06
	 * @since 2010.08.06 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void gotoFIinAddress() {
		final String sUrl= cJTxFdAddress.getText(); //= sUrlDisplayed
		String sFI= "";

		if (sUrl.indexOf("#")==-1) {
			JTextComponent cJTxtComp= (JTextComponent)cBrsr;
			if (cJTbPnR.getSelectedIndex()==1)
				cJTxtComp= (JTextComponent)cEdWw;
			else if (cJTbPnR.getSelectedIndex()==2)
				cJTxtComp= (JTextComponent)cEdSrc;
			cJTxtComp.setCaretPosition(0);
			return;
		}
		else {
			sFI= sUrl.substring(sUrl.indexOf("#")+1);

			if (cJTbPnR.getSelectedIndex()==2){
				if (sFI.startsWith("h0")) {
					if (sFI.indexOf("p")!=-1)
						findText("#"+sFI+"#", true, 0);
					else
						findText("#"+sFI+" h", true, 0);
				}
				else
					findText("\""+sFI+"\"", true, 0);
			}
			else {
				if (sFI.startsWith("h0"))
					findText(sFI, true, 0);
				else {
					try {
						if (cJTbPnR.getSelectedIndex()==0)
							cBrsr.setPage(sUrl);
						else if (cJTbPnR.getSelectedIndex()==1)
							cEdWw.setPage(sUrl);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//not SFIs are not inside the text.
//					SwingUtilities.invokeLater(new Runnable() {
//						public void run(){
//							try {
//								if (cJTbPnR.getSelectedIndex()==0)
//									cBrsr.setPage(sUrl);
//								else if (cJTbPnR.getSelectedIndex()==1)
//									cEdWw.setPage(sUrl);
//								selectTreeNode(sUrl);
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					});
				}
			}
		}
	}


	/**
	 *
	 * @modified 2010.08.12
	 * @since 2010.08.12 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void initialize() {
		historyBF = new HtmlHistoryBackForward();
		lvUrlFilesToc = new Vector<String>();
		phUrlTitle= new Hashtable<String,String>();
		phUrlTrNode= new Hashtable<String,DefaultMutableTreeNode>();
		phWord_UrlTS= new Hashtable<String,TreeSet<String>>();
	}


	/**
	 * Inserts html-code in the wysiwyg-editor.
	 *
	 * @modified 2010.08.05
	 * @since 2010.08.05 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void insertHtml(String sHtml, int niLocation) //throws BadLocationException, IOException
	{
		//assumes editor is already set to "text/html" type
		HTMLEditorKit kit = (HTMLEditorKit) cEdWw.getEditorKit();
		HTMLDocument hDoc= (HTMLDocument)cEdWw.getDocument();//WW-DOC
		StringReader reader = new StringReader(sHtml);
		try {
			kit.read(reader, hDoc, niLocation);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}


	/**
	 *
	 * @modified 2010.08.05
	 * @since 2010.08.05 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void insertStyleSheet(JEditorPane jep){
		HTMLDocument htmlDoc= (HTMLDocument)jep.getDocument();
		StyleSheet ss = htmlDoc.getStyleSheet();
		try {
			ss.importStyleSheet(new URL("file:g:/file1/aajworking/aaj.css"));
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		htmlDoc = new HTMLDocument(ss);
		jep.setDocument(htmlDoc);
		htmlDoc.addDocumentListener(lsnDoc);
	}


	/**
	 * Returns an array of file-names in a directory
	 * recursively or not.
	 *
	 * @modified 2010.04.25
	 * @since 2010.04.25 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public static String[] listFileNamesAsArray(
					File directory,
					FilenameFilter filter,
					boolean recurse)
	{
		Collection<File> files = listFiles(directory,
				filter, recurse);

		File[] arr = new File[files.size()];
		arr= files.toArray(arr);

		String[] arr2 = new String[arr.length];
		for (int i=0; i<arr.length; i++)
			arr2[i]= arr[i].toString();
		return arr2;
	}

	/**
	 *
	 * @modified 2010.04.26
	 * @since 2010.04.26 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	public static Collection<File> listFiles(
				File directory,
				FilenameFilter filter,
				boolean recurse)
	{
		// List of files / directories
		Vector<File> files = new Vector<File>();

		// Get files / directories in the directory
		File[] entries = directory.listFiles();

		// Go over entries
		for (File entry : entries)
		{
			// If there is no filter or the filter accepts the
			// file / directory, add it to the list
			if (filter == null || filter.accept(directory, entry.getName()))
			{
System.out.println(entry.getAbsoluteFile().toString());//hhh
				files.add(entry.getAbsoluteFile());
			}

			// If the file is a directory and the recurse flag
			// is set, recurse into the directory
			if (recurse && entry.isDirectory())
			{
				files.addAll(listFiles(entry, filter, recurse));
			}
		}
		// Return collection of files
		return files;
	}


	/**
	 *
	 * @modified 2010.09.12
	 * @since 2010.04.29 (v00.02.02)
	 * @author Slava Pestov
	 */
	public static Icon loadIcon(String iconName)
	{
		//javaws nnn iii ppp
//		java.net.URL urlImg = HtmlMgr.class.getResource("/icons/"+iconName);
//		if (urlImg != null)
//			return new ImageIcon(urlImg);
//		else
//			return null;

		JarResources jarRs = new JarResources(
															sDirHome+"HtmlMgr-icons-2010.09.13.jar");// ddd
		Image imgNew = Toolkit.getDefaultToolkit().createImage(
																	jarRs.getResource("icons/"+iconName));
		return new ImageIcon(imgNew);
	}


	/**
	 *
	 * @modified 2010.08.21
	 * @since 2010.08.21 (v00.02.03)
	 * @author http://sourceforge.net/projects/browserlaunch2/
	 */
	public void openBrowser(String sUrl) {
		String osName = System.getProperty("os.name");
		try {
//			if (osName.startsWith("Mac OS")) {
//				Class fileMgr = Class.forName("com.apple.eio.FileManager");
//				Method openURL = fileMgr.getDeclaredMethod("openURL",
//													new Class[] {String.class});
//				openURL.invoke(null, new Object[] {sUrl});
//			}
			if (osName.startsWith("Windows"))
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + sUrl);
			else { //assume Unix or Linux
				String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0)
						browser = browsers[count];
					if (browser == null)
						throw new Exception("Could not find web browser");
					else
						Runtime.getRuntime().exec(new String[] {browser, sUrl});
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error attempting to launch web browser:\n" + e.getLocalizedMessage());
		}
	}


	/**
	 * Reload the current URL, from hardDisk.
	 *
	 * @modified 2010.08.10
	 * @since 2010.04.30 (v00.02.02)
	 * @author Dirk Moebius
	 */
	private void reloadUrl()
	{
		if (sUrlDisplayed == null){
System.out.println(">> no-titled-url");
			return;
		}

		String sUrlReload= sUrlDisplayed;
		createTocFile(getUrlFileOfUrl(sUrlReload));
		displayUrl(sUrlReload, false);
	}


	/**
	 *
	 * @modified 2010.04.27
	 * @since 2010.04.27 (v00.02.02)
	 * @author Slava Pestov
	 */
	public void selectTreeNode(String sUrl)
	{
		if(cTocTrMdl == null)
			return;

//		sUrl= sUrl.toLowerCase();//because "relative-locations are UpperCase"

		String sFI= HtmlUtilities.getFIfromUrlString(sUrl);
		String sFInon= sUrl;
		if (sUrl.indexOf("#")!=-1)
			sFInon= sUrl.substring(0,sUrl.indexOf("#"));
		if (!sFI.startsWith("h0") && !sFI.equals("")){
			String sSFI= findSFIofVisibleElement();
			sUrl= sFInon+"#"+sSFI;
		}

		if (sUrl.indexOf(" h")!=-1) {
			//h0.2.1.3 h3
			sUrl= sUrl.substring(0, sUrl.indexOf(" h"));
		}

		DefaultMutableTreeNode node = phUrlTrNode.get(sUrl);
		if(node == null){
			if (sUrl.indexOf("#")!=-1)
				node= phUrlTrNode.get(sFInon);
			if(node == null)
				return;
		}

		TreePath path = new TreePath(cTocTrMdl.getPathToRoot(node));
//System.out.println(">>>TreePath= "+path.toString());
		cTocJTr.expandPath(path);
		cTocJTr.setSelectionPath(path);
		cTocJTr.scrollPathToVisible(path);
		//trick wrka to set X axis at 0.
		Rectangle bounds = cTocJTr.getPathBounds(path);
		if(bounds != null) {
			bounds.setBounds(0, (int)bounds.getY(),
						(int)bounds.getWidth(), (int)bounds.getHeight());
			cTocJTr.scrollRectToVisible(bounds);
		}
	}


	/**
	 * Sets a property to a new value.
	 *
	 * @param name The property
	 * @param value The new value
	 * @author Slava Pestov
	 */
	public static void setProperty(String name, String value)
	{
		propMgr.setProperty(name,value);
	}


	/**
	 * Sets the text of a text-component (JEditorPane, JTextArea).
	 * First removes the listeners, sets the text, and then
	 * adds the listeners.
	 *
	 * @modified 2010.08.12
	 * @since 2010.08.12 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void setText(JTextComponent cJTxt, String sTxt){
//		cJTxt.setMaximumSize(new Dimension(1100, 1100));
		cJTxt.getDocument().removeDocumentListener(lsnDoc);
		cJTxt.getDocument().removeUndoableEditListener(lsnUndo);
		cJTxt.removePropertyChangeListener("page",lsnPropEdWw);

		//set text
		cJTxt.setText(sTxt);

		cJTxt.getDocument().addDocumentListener(lsnDoc);
		cJTxt.getDocument().addUndoableEditListener(lsnUndo);
	}


	/**
	 * Sets the title of the application's window.
	 *
	 * @modified 2010.08.12
	 * @since 2010.05.02 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void setWindowTitle(String sTitle)
	{
		if (sTitle==null || sTitle.equals(""))
			cJFrHtmlMgr.setTitle("HtmlMgr");
		else if (sTitle.indexOf("(modified-)")!=-1)
			cJFrHtmlMgr.setTitle(sTitle);
		else
			cJFrHtmlMgr.setTitle(sTitle+ " - HtmlMgr");
	}


	/**
	 * Sets in the application's window-title, which editor is modified.
	 *
	 * @param niEd	Integer for editor: 1 for WW, 2 for Source.
	 * 			0 removes "modified".
	 * @modified 2010.08.07
	 * @since 2010.07.06 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void setWindowTitleModified(int niEd)
	{
		String sTitle = (String)cBrsr.getDocument().getProperty("title");
		if (sTitle == null)
			sTitle = getFileName(cBrsr.getPage().toString());

		if (niEd==0)
			cJFrHtmlMgr.setTitle(sTitle +" - HtmlMgr");
		else if (niEd==1)
			cJFrHtmlMgr.setTitle(sTitle +" - HtmlMgr (modified-WysiWyg)");
		else if (niEd==2)
			cJFrHtmlMgr.setTitle(sTitle +" - HtmlMgr (modified-Source)");
	}


	/**
	 * Puts in the pair {url,title} data.  If url is directory, as title
	 * sets the name of the directory, not its path.
	 *
	 * @modified 2010.05.21
	 * @since 2010.05.21 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private static void storeTitle(String sUrl, String sTitle){
		if (sTitle==null){ //a dir-node
			if (sUrl.lastIndexOf("/")!=-1)
				phUrlTitle.put(sUrl,sUrl.substring(sUrl.lastIndexOf("/")+1));
			else if (sUrl.lastIndexOf("\\")!=-1)
				phUrlTitle.put(sUrl,sUrl.substring(sUrl.lastIndexOf("\\")+1));
			else
				phUrlTitle.put(sUrl,sUrl);
		} else
			phUrlTitle.put(sUrl,sTitle);
	}


	/**
	 *
	 * @param niTab	The integers 0, 1, 2 denoted the browser, ww-editor
	 * 		and source editor.
	 * @modified 2010.08.09
	 * @since 2010.07.10 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	private void synchronizeTabR(int niTab)
	{
		//BROWSER tab
		if (niTab==0){
//			cBrsr.setMaximumSize(new Dimension(1100, 1100));
			if (bModified2){
				//display source-editor-content
				cBrsr.setText(cEdSrc.getText());
			}
			else if (bModified1){
				cBrsr.setText(cEdWw.getText());
			}
		}

		//WW-EDITOR tab
		else if (niTab==1) {
			if (bModified2){
				setText(cEdWw, cEdSrc.getText());
			}
		}

		//SOURCE-EDITOR tab
		else if (niTab==2) {
			if (bModified1){
				setText(cEdSrc, getSfiHtml(cEdWw));
			}
		}
	}

	/**
	 *
	 * @modified 2010.05.03
	 * @since 2010.05.03 (v00.02.02)
	 * @author Dirk Moebius
	 */
	private void updateBFButtons()
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				actGBack.setEnabled(historyBF.hasPrevious());
				actGForward.setEnabled(historyBF.hasNext());
			}
		});
	}


	/**
	 *
	 * @modified 2010.04.30
	 * @since 2010.04.30 (v00.02.02)
	 * @author Dirk Moebius
	 */
	private synchronized void updateGotoMenu()
	{
		cJMnGoto.removeAll();
		cJMnGoto.add(actGBack);
		cJMnGoto.add(actGForward);
		cJMnGoto.add(actGHome);
		cJMnGoto.add(actGReload);

		cJMnGoto.add(new JSeparator());
		cJMnGoto.add(actGFind);

		cJMnGoto.add(new JSeparator());
		cJMnGoto.add(actGListDir);

		cJMnGoto.add(new JSeparator());
		// add history
		String[] entr = historyBF.getGoMenuEntries();
		for (int i = 0; i < entr.length; i++)
		{
			JMenuItem mi = new JMenuItem(entr[i],
				entr[i].equals(sUrlDisplayed) ? iconCheck : iconNoCheck);
			mi.setActionCommand(entr[i]);
			mi.addActionListener(lsnActUBHistory);
			cJMnGoto.add(mi);
		}

		// add Files visited
		cJMnGoto.add(new JSeparator());
		for (String sUF : ltsUrlFilesVisited){
			JMenuItem jmi = new JMenuItem(sUF);
			jmi.setActionCommand(sUF);
			jmi.addActionListener(new ListenerActionURLButton(false));
			cJMnGoto.add(jmi);
		}
	}


	/**
	 * INPUT: a string of html with broken-SFIs.<br/>
	 * OUTPUT: the input string with non-broken-SFIs.
	 *
	 * @modified 2010.08.12
	 * @since 2010.08.12 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	public String updateSFI(String sSFIb) {
		HtmlUpdateSFI hus= new HtmlUpdateSFI(sSFIb, false);
		return hus.sOut;
	}


	/**
	 * Writes the index of words|terms in the file "index.txt"
	 * in the home directory.
	 *
	 * @modified 2010.05.24
	 * @since 2010.05.24 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	private void writeIndex(){
		try {
			FileWriter writer = new FileWriter("index.txt");
			BufferedWriter bw =new BufferedWriter(writer);

			Set<String> setK= 	phWord_UrlTS.keySet();
			for (String s : setK){
				bw.write(s);
				bw.newLine();
			}
			bw.close();
		} catch (IOException ioe) {
			System.out.println("!!!HtmlMgr.writeIndex: "+ioe.toString());
		}
	}



	/**
	 *
	 * @modified 2010.08.14
	 * @since 2010.08.14 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditCopy extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditCopy() {
			super("hm.actECopy");
		}

		public void actionPerformed(ActionEvent evt) {
			JTextComponent cJTxToEdit= (JTextComponent)cBrsr;
			if (cJTbPnR.getSelectedIndex()==1)
				cJTxToEdit= (JTextComponent)cEdWw;
			if (cJTbPnR.getSelectedIndex()==2)
				cJTxToEdit= (JTextComponent)cEdSrc;

			cJTxToEdit.paste();
		}
	}



	/**
	 *
	 * @modified 2010.08.14
	 * @since 2010.08.14 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditCut extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditCut() {
			super("hm.actECut");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0)
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));

			JTextComponent cJTxToEdit= (JTextComponent)cEdWw;
			if (cJTbPnR.getSelectedIndex()==2)
				cJTxToEdit= (JTextComponent)cEdSrc;

			cJTxToEdit.cut();
		}
	}



	/**
	 *
	 * @modified 2010.08.14
	 * @since 2010.08.14 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditFormatBold extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditFormatBold() {
			super("hm.actEFrmBold");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0)
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));
			else if (cJTbPnR.getSelectedIndex()==1){
				int niS= cEdWw.getSelectionStart();
				int niE= cEdWw.getSelectionEnd();
				HTMLDocument hDoc= (HTMLDocument)cEdWw.getDocument();
				HtmlEditorKitEditorWW hKit= ((HtmlEditorKitEditorWW)cEdWw.getEditorKit());
				String sSelection= cEdWw.getSelectedText();
				if (sSelection == null) {
					JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectText"));
					return;
				}
				sSelection= "<b>"+sSelection+"</b>";
				cEdWw.cut();
				try {
					hKit.insertHTML(hDoc,niS, sSelection, 0, 0, HTML.Tag.B);
					cEdWw.setCaretPosition(niE);
				} catch (BadLocationException ble) {
					System.out.println(ble.toString());
				} catch (IOException ioe) {
					System.out.println(ioe.toString());
				}
			}
			else if (cJTbPnR.getSelectedIndex()==2){
				int niS= cEdSrc.getSelectionStart();
				int niE= cEdSrc.getSelectionEnd();
				String sSelection= cEdSrc.getSelectedText();
				if (sSelection == null) {
					JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectText"));
					return;
				}
				sSelection= "<b>"+sSelection+"</b>";
				cEdSrc.cut();
				cEdSrc.insert(sSelection, niS);
				cEdSrc.setCaretPosition(niS +sSelection.length());
			}
		}
	}



	/**
	 *
	 * @modified 2010.08.15
	 * @since 2010.08.15 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditFormatItalic extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditFormatItalic() {
			super("hm.actEFrmItalic");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0)
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));
			else if (cJTbPnR.getSelectedIndex()==1){
				int niS= cEdWw.getSelectionStart();
				int niE= cEdWw.getSelectionEnd();
				HTMLDocument hDoc= (HTMLDocument)cEdWw.getDocument();
				HtmlEditorKitEditorWW hKit= ((HtmlEditorKitEditorWW)cEdWw.getEditorKit());
				String sSelection= cEdWw.getSelectedText();
				if (sSelection == null) {
					JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectText"));
					return;
				}
				sSelection= "<i>"+sSelection+"</i>";
				cEdWw.cut();
				try {
					hKit.insertHTML(hDoc,niS, sSelection, 0, 0, HTML.Tag.I);
					cEdWw.setCaretPosition(niE);
				} catch (BadLocationException ble) {
					System.out.println(ble.toString());
				} catch (IOException ioe) {
					System.out.println(ioe.toString());
				}
			}
			else if (cJTbPnR.getSelectedIndex()==2){
				int niS= cEdSrc.getSelectionStart();
				int niE= cEdSrc.getSelectionEnd();
				String sSelection= cEdSrc.getSelectedText();
				if (sSelection == null) {
					JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectText"));
					return;
				}
				sSelection= "<i>"+sSelection+"</i>";
				cEdSrc.cut();
				cEdSrc.insert(sSelection, niS);
				cEdSrc.setCaretPosition(niS +sSelection.length());
			}
		}
	}



	/**
	 *
	 * @modified 2010.08.15
	 * @since 2010.08.15 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditFormatUnderline extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditFormatUnderline() {
			super("hm.actEFrmUnderline");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0)
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));
			else if (cJTbPnR.getSelectedIndex()==1){
				int niS= cEdWw.getSelectionStart();
				int niE= cEdWw.getSelectionEnd();
				HTMLDocument hDoc= (HTMLDocument)cEdWw.getDocument();
				HtmlEditorKitEditorWW hKit= ((HtmlEditorKitEditorWW)cEdWw.getEditorKit());
				String sSelection= cEdWw.getSelectedText();
				if (sSelection == null) {
					JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectText"));
					return;
				}
				sSelection= "<span class=\"u\">"+sSelection+"</span>";
				cEdWw.cut();
				try {
					hKit.insertHTML(hDoc,niS, sSelection, 0, 0, HTML.Tag.SPAN);
					cEdWw.setCaretPosition(niE);
				} catch (BadLocationException ble) {
					System.out.println(ble.toString());
				} catch (IOException ioe) {
					System.out.println(ioe.toString());
				}
			}
			else if (cJTbPnR.getSelectedIndex()==2){
				int niS= cEdSrc.getSelectionStart();
				int niE= cEdSrc.getSelectionEnd();
				String sSelection= cEdSrc.getSelectedText();
				if (sSelection == null) {
					JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectText"));
					return;
				}
				sSelection= "<span class=\"u\">"+sSelection+"</span>";
				cEdSrc.cut();
				cEdSrc.insert(sSelection, niS);
				cEdSrc.setCaretPosition(niS +sSelection.length());
			}
		}
	}



	/**
	 * Deletes a Heading or Paragraph element.
	 *
	 * @modified 2010.09.03
	 * @since 2010.09.03 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditDeleteElement extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditDeleteElement() {
			super("hm.actEDelElement");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0)
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));
			else if (cJTbPnR.getSelectedIndex()==1){
				int niCaretPos = cEdWw.getCaretPosition();
				HTMLDocument hDoc= (HTMLDocument)cEdWw.getDocument();
				Element elem = hDoc.getParagraphElement(niCaretPos);
				niCaretPos= elem.getStartOffset();

				cEdWw.requestFocus();
				cEdWw.select(elem.getStartOffset(),elem.getEndOffset());
				cEdWw.cut();
				createTocHtml(updateSFI(getSfiHtml(cEdWw)));
				cEdWw.setCaretPosition(niCaretPos);

//				try {
//					hDoc.remove(elem.getStartOffset(),elem.getEndOffset());
//					cEdWw.setCaretPosition(elem.getEndOffset()+2);
//					//display
//					createTocHtml(updateSFI(getSfiHtml(cEdWw)));
//					cEdWw.setCaretPosition(niCaretPos);
//				} catch (Exception e) {
//					System.out.println("!!!ex: ");
//					e.printStackTrace();
//				}
			}

			else if (cJTbPnR.getSelectedIndex()==2){
				int niP= cEdSrc.getCaretPosition();
				PlainDocument docPl= (PlainDocument)cEdSrc.getDocument();
				Element eRoot= docPl.getDefaultRootElement();
				int niIdxCh= eRoot.getElementIndex(niP);
				int niStart= 0;
				int niEnd= 0;
				Element eP;
				String sTx= "";
				for (int i=niIdxCh; i>0; i--){
					//finds the beginning of element of cursor
					eP= eRoot.getElement(i);
					sTx= getTextOfElement(docPl,eP);
					if (sTx.startsWith("<h1>") || sTx.startsWith("<h2>")
						||sTx.startsWith("<h3>") || sTx.startsWith("<h4>")
						||sTx.startsWith("<h5>") || sTx.startsWith("<h6>")
						||sTx.startsWith("<p>") || sTx.indexOf("<p class=")!=-1 )
					{
						niStart= eP.getStartOffset();
						break;
					}
				}
				for (int j=niIdxCh; j<eRoot.getElementCount(); j++) {
					//finds the END of element of cursor
					eP= eRoot.getElement(j);
					sTx= getTextOfElement(docPl,eP);
					if (sTx.startsWith("</h1>") || sTx.startsWith("</h2>")
						||sTx.startsWith("</h3>") || sTx.startsWith("</h4>")
						||sTx.startsWith("</h5>") || sTx.startsWith("</h6>")
						||sTx.startsWith("</p>")  )
					{
						niEnd= eP.getEndOffset();
						break;
					}
				}
				cEdSrc.requestFocus();
				cEdSrc.select(niStart,niEnd);
				cEdSrc.cut();
				createTocHtml(updateSFI(cEdSrc.getText()));
				cEdSrc.setCaretPosition(niStart);
			}
		}
	}



	/**
	 * Inserts an html-break element at caret.
	 *
	 * @modified 2010.08.10
	 * @since 2010.08.10 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditInsertBreak extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditInsertBreak() {
			super("hm.actEInsBreak");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0)
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));
			else if (cJTbPnR.getSelectedIndex()==1){
				int niCaretPos = cEdWw.getCaretPosition();
				HTMLDocument hDoc= (HTMLDocument)cEdWw.getDocument();
				HtmlEditorKitEditorWW hKit= ((HtmlEditorKitEditorWW)cEdWw.getEditorKit());
				try {
					hKit.insertHTML(hDoc,niCaretPos, "<br/>", 0, 0, HTML.Tag.BR);
					cEdWw.setCaretPosition(niCaretPos + 1);
				} catch (BadLocationException ble) {
					System.out.println(ble.toString());
				} catch (IOException ioe) {
					System.out.println(ioe.toString());
				}
			}
			else if (cJTbPnR.getSelectedIndex()==2){
				int niCaretPos = cEdSrc.getCaretPosition();
				cEdSrc.insert("\n  <br/>",niCaretPos);
			}
		}
	}



	/**
	 * Inserts an SFI-heading-element, at the END of the SFI-element
	 * (heading or paragraph) the caret is on.
	 *
	 * @modified 2010.08.12
	 * @since 2010.08.10 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditInsertHeading extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditInsertHeading() {
			super("hm.actEInsHeading");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0) {
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));
				return;
			}
			//choose heading
			String sH= "";
			Object[] options = {
									"Heanding1",
									"Heanding2",
									"Heanding3",
									"Heanding4",
									"Heanding5",
									"Heanding6" };
			Object selectedValue = JOptionPane.showInputDialog(null,
						"What Heading you want to insert?",					//message
						"Choose One",															//title
						JOptionPane.QUESTION_MESSAGE,								//message type
						null,																			//icon
						options,																		//values to select
						options[0]);																//initial selection
			if (selectedValue!=null)
				sH= (String) selectedValue;

			if (cJTbPnR.getSelectedIndex()==1){
				int niCaretPos = cEdWw.getCaretPosition();
				HTMLDocument hDoc= (HTMLDocument)cEdWw.getDocument();
				Element elem = hDoc.getParagraphElement(niCaretPos);
				try {
					if (sH.equals("Heanding1"))
						hDoc.insertAfterEnd(elem, "<h1>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h1>");
					else if (sH.equals("Heanding2"))
						hDoc.insertAfterEnd(elem, "<h2>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h2>");
					else if (sH.equals("Heanding3"))
						hDoc.insertAfterEnd(elem, "<h3>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h3>");
					else if (sH.equals("Heanding4"))
						hDoc.insertAfterEnd(elem, "<h4>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h4>");
					else if (sH.equals("Heanding5"))
						hDoc.insertAfterEnd(elem, "<h5>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h5>");
					else if (sH.equals("Heanding6"))
						hDoc.insertAfterEnd(elem, "<h6>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h6>");
//					cEdWw.setCaretPosition(elem.getEndOffset()+2);
					//display
					createTocHtml(updateSFI(getSfiHtml(cEdWw)));
					cEdWw.setCaretPosition(niCaretPos+3);
				} catch (Exception e) {
					System.out.println(e.toString());
				}
			}

			else if (cJTbPnR.getSelectedIndex()==2){
				int niCaretPos = cEdSrc.getCaretPosition();
				//find the offset of the END of current h or p element
				int aPos[]= findRegEx("\\n</.+>", niCaretPos);
				try {
					if (sH.equals("Heanding1"))
						cEdSrc.insert("\n<h1>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h1>", aPos[1]);
					else if (sH.equals("Heanding2"))
						cEdSrc.insert("\n<h2>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h2>", aPos[1]);
					else if (sH.equals("Heanding3"))
						cEdSrc.insert("\n<h3>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h3>", aPos[1]);
					else if (sH.equals("Heanding4"))
						cEdSrc.insert("\n<h4>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h4>", aPos[1]);
					else if (sH.equals("Heanding5"))
						cEdSrc.insert("\n<h5>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h5>", aPos[1]);
					else if (sH.equals("Heanding6"))
						cEdSrc.insert("\n<h6>\n  <a name=\"h0.1\"></a>\n  txt\n  <a class=\"hide\">#h0.1#</a>\n</h6>", aPos[1]);

					//update SFIs for this doc and display it
					createTocHtml(updateSFI(cEdSrc.getText()));
					cEdSrc.setCaretPosition(niCaretPos+3);
				} catch (Exception e) {
					System.out.println(e.toString());
				}
			}

		}
	}



	/**
	 * On WW-editor: If the curson is on a link, display the destination
	 * to edit it. Otherewise asks for selected-text and destination.<br/>
	 * On source-editor asks for selected-text.
	 *
	 * @modified 2010.08.17
	 * @since 2010.08.17 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditInsertHyperlink extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditInsertHyperlink() {
			super("hm.actEInsHyperlink");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0)
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));
			else if (cJTbPnR.getSelectedIndex()==1){

				int niCaretPos = cEdWw.getCaretPosition();
				HTMLDocument hDoc= (HTMLDocument)cEdWw.getDocument();
				HtmlEditorKitEditorWW hKit= ((HtmlEditorKitEditorWW)cEdWw.getEditorKit());
				Element elem = hDoc.getCharacterElement(niCaretPos);
				Hashtable<String,String> htAttrs= new Hashtable<String,String>();
				AttributeSet attrs = elem.getAttributes();
				Enumeration keys=attrs.getAttributeNames();
				while( keys.hasMoreElements()) {
					Object key= keys.nextElement();
					Object value= attrs.getAttribute(key);
					htAttrs.put(key.toString(), value.toString());
				}
				if (htAttrs.containsKey("a")){
					//	a,href=www.xxx#yyy
					String sAValue= htAttrs.get("a");
					String sAVValue= sAValue.substring(sAValue.indexOf("=")+1).trim();
					if (sAValue.startsWith("href=")){
						String sDest= "";
						String sElemTxt="";
						int niS= elem.getStartOffset();
						int niE= elem.getEndOffset();
						try {
							sElemTxt= hDoc.getText(niS, niE-niS);
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
						String sInput = JOptionPane.showInputDialog(
							getProperty("hm.txt.EnterDestination"), //message
							sAVValue); //initial
						if(sInput != null) {
							sDest= sInput;
							try {
								hDoc.remove(niS, niE-niS);
							} catch (BadLocationException e2) {
								e2.printStackTrace();
							}
							String sHtml= "<a href=\"" +sDest +"\">" +sElemTxt +"</a>";
							try {
								hKit.insertHTML(hDoc,niS, sHtml, 0, 0, HTML.Tag.A);
								cEdWw.setCaretPosition(niE);
							} catch (BadLocationException ble) {
								System.out.println(ble.toString());
							} catch (IOException ioe) {
								System.out.println(ioe.toString());
							}
						}
					}
				}
				else if (!htAttrs.containsKey("a")){
					int niS= cEdWw.getSelectionStart();
					int niE= cEdWw.getSelectionEnd();
					String sSelection= cEdWw.getSelectedText();
					if (sSelection == null) {
						JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectText"));
						return;
					}
					String sDest= "";
					String sInput = JOptionPane.showInputDialog(
						getProperty("hm.txt.EnterDestination"), //message
						sSelection); //initial
					if(sInput != null) {
						sDest= sInput;
						cEdWw.cut();
						String sHtml= "<a href=\"" +sDest +"\">" +sSelection +"</a>";
						try {
							hKit.insertHTML(hDoc,niS, sHtml, 0, 0, HTML.Tag.A);
							cEdWw.setCaretPosition(niE);
						} catch (BadLocationException ble) {
							System.out.println(ble.toString());
						} catch (IOException ioe) {
							System.out.println(ioe.toString());
						}
					}
				}
			}
			else if (cJTbPnR.getSelectedIndex()==2){
				int niS= cEdSrc.getSelectionStart();
				int niE= cEdSrc.getSelectionEnd();
				String sSelection= cEdSrc.getSelectedText();
				if (sSelection == null) {
					JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectText"));
					return;
				}
				String sDest= "";
				String sInput = JOptionPane.showInputDialog(
					getProperty("hm.txt.EnterDestination"), //message
					sSelection); //initial
				if(sInput != null) {
					sDest= sInput;
					cEdSrc.cut();
					String sHtml= "<a href=\"" +sDest +"\">" +sSelection +"</a>";
					cEdSrc.insert(sHtml, niS);
					cEdSrc.setCaretPosition(niS +sHtml.length());
				}
			}
		}
	}



	/**
	 * Inserts an SFI-Paragraph-element, at the END of the SFI-element
	 * (heading or paragraph) the caret is on.
	 *
	 * @modified 2010.08.12
	 * @since 2010.08.10 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditInsertParagraph extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditInsertParagraph() {
			super("hm.actEInsParagraph");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0)
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));
			else if (cJTbPnR.getSelectedIndex()==1){
				int niCaretPos = cEdWw.getCaretPosition();
				HTMLDocument hDoc= (HTMLDocument)cEdWw.getDocument();
				Element elem = hDoc.getParagraphElement(niCaretPos);
				try {
					hDoc.insertAfterEnd(elem, "<p>\n  <a name=\"h0.1p1\"></a>\n  txt\n  <a class=\"hide\">#h0.1p1#</a>\n</p>");
//					cEdWw.setCaretPosition(elem.getEndOffset()+2);
					//display
					createTocHtml(updateSFI(getSfiHtml(cEdWw)));
					cEdWw.setCaretPosition(cEdWw.getCaretPosition()+4);
				} catch (Exception e) {
					System.out.println(e.toString());
				}
			}

			else if (cJTbPnR.getSelectedIndex()==2){
				int niCaretPos = cEdSrc.getCaretPosition();
				//find the offset of the END of current h or p element
				int aPos[]= findRegEx("\\n</.+>", niCaretPos);
				try {
					cEdSrc.insert("\n<p>\n  <a name=\"h0.1p1\"></a>\n  txt\n  <a class=\"hide\">#h0.1p1#</a>\n</p>", aPos[1]);
//					cEdSrc.setCaretPosition(aPos[1]+30);
					//update SFIs for this doc and display it
					createTocHtml(updateSFI(cEdSrc.getText()));
					cEdSrc.setCaretPosition(niCaretPos+3);
				} catch (Exception e) {
					System.out.println(e.toString());
				}
			}
		}
	}



	/**
	 *
	 * @modified 2010.08.14
	 * @since 2010.08.14 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditPaste extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditPaste() {
			super("hm.actEPaste");
		}

		public void actionPerformed(ActionEvent evt) {
			if (cJTbPnR.getSelectedIndex()==0)
				JOptionPane.showMessageDialog(cJFrHtmlMgr, getProperty("hm.txt.SelectEditor"));

			JTextComponent cJTxToEdit= (JTextComponent)cEdWw;
			if (cJTbPnR.getSelectedIndex()==2)
				cJTxToEdit= (JTextComponent)cEdSrc;

			cJTxToEdit.paste();
		}
	}



	/**
	 *
	 * @modified 2010.08.11
	 * @since 2010.08.11 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditRedo extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditRedo() {
			super("hm.actERedo");
			setEnabled(false);
		}
		public void actionPerformed(ActionEvent e) {
			try {
				undoMgr.redo();
			} catch (CannotRedoException ex) {
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			update();
			actEUndo.update();
		}

		public void update() {
			if(undoMgr.canRedo()) {
				setEnabled(true);
//				putValue(Action.NAME, undoMgr.getRedoPresentationName());
			}
			else {
				setEnabled(false);
//				putValue(Action.NAME, "actERedo");
			}
		}
	}



	/**
	 *
	 * @modified 2010.08.11
	 * @since 2010.08.11 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditUndo extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditUndo() {
			super("hm.actEUndo");
			setEnabled(false);
		}
		public void actionPerformed(ActionEvent e) {
			try {
				undoMgr.undo();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null,
					"!!!ex: Unable to undo",
					"Warning", JOptionPane.WARNING_MESSAGE);
				ex.printStackTrace();
				//we could reload the previous backup-file ppp
				return;
			}
			update();
			actERedo.update();
		}

		public void update() {
			if(undoMgr.canUndo()) {
				setEnabled(true);
			}
			else {
				setEnabled(false);
				setWindowTitleModified(0);
			}
		}
	}



	/**
	 * Asks for an SFI-file and updates broken SFIs.
	 *
	 * @modified 2010.08.03
	 * @since 2010.08.03 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionEditUpdateSFI extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionEditUpdateSFI() {
			super("hm.actEUpdateSFI");
		}

		public void actionPerformed(ActionEvent evt) {
			FileDialog fileDialog = new FileDialog(cJFrHtmlMgr, getDirHome());
			fileDialog.setMode(FileDialog.LOAD);
			fileDialog.setVisible(true);
			String file = fileDialog.getFile();
			if (file ==	null)	{
				return;
			}
			File f = new File(fileDialog.getDirectory(), file);
			if (f.exists())	{
				String sFile= f.getAbsolutePath();
				String sUrl= HtmlUtilities.createUrlString(sFile);
				HtmlUpdateSFI husfi= new HtmlUpdateSFI(sUrl, true);
			}
		}
	}




	/**
	 * Closes the program.
	 *
	 * @modified 2010.04.30
	 * @since 2010.04.30 (v00.02.02)
	 * @author Dirk Moebius
	 */
	class ActionFileExit extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionFileExit() {
			super("hm.actFExit");
		}

		public void actionPerformed(ActionEvent evt) {
			//save variables
			System.exit(-1);
		}
	}



	/**
	 * Creates a new SFI-html-file.
	 *
	 * @modified 2010.09.12
	 * @since 2010.07.05 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionFileNew extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionFileNew() {
			super("hm.actFNew");
		}

		public void actionPerformed(ActionEvent evt) {
			String sHtml=
					 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+"\n<!DOCTYPE html"
					+"\n  PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
					+"\n  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
					+"\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
					+"\n<head>"
					+"\n  <title>Write-WINDOW-title-in-SourceEditor</title>"
					+"\n  <link href=\"AAj-2010.08.17.css\" rel=\"stylesheet\" type=\"text/css\"/>"
					+"\n</head>"
					+"\n<body>"
					+"\n<p class=\"h0\">"
					+"\n  <a name=\"h0\"></a>"
					+"\n  write-DOC-title"
					+"\n  <a class=\"hide\">#h0#</a>"
					+"\n</p>"
					+"\n<p>"
					+"\n  <a name=\"h0p1\"></a>"
					+"\n  Para ..."
					+"\n  <a class=\"hide\">#h0p1#</a>"
					+"\n</p>"
					+"\n<h1>"
					+"\n  <a name=\"h0.toc\"></a>"
					+"\n  Table-of-Contents"
					+"\n  <a class=\"hide\">#h0.toc#</a>"
					+"\n</h1>"
					+"\n<p class=\"notoc\">"
					+"\n  Home ..."
					+"\n  <a class=\"hide\">#h0.toc#</a>"
					+"\n</p>"
					+"\n<hr/>"
					+"\n<p class=\"last\">"
					+"\n  This is an <a href=\"http://htmlmgr.sourceforge.net/index.html#ifiSFI\">SFI-file</a>, best viewed with <a href=\"http://htmlmgr.sourceforge.net/\">HtmlMgr</a>."
					+"\n  <a class=\"hide\">#h0.toc#</a>"
					+"\n</p>"
					+"\n<p class=\"last\">"
					+"\n  VERSIONS:"
					+"\n  <br/>CURRENT: tmp-"+HtmlUtilities.setCurrentDate()+".html"
					+"\n  <br/>CREATED: tmp-"+HtmlUtilities.setCurrentDate()+".html"
					+"\n  <br/>MAIL: "
					+"\n  <a class=\"hide\">#h0.toc#</a>"
					+"\n</p>"
					+"\n</body>"
					+"\n</html>";

			cJTbPnR.setSelectedIndex(1);
			String sNewUrl= HtmlUtilities.createUrlString(sDirHome+"sfi-tmp-"+HtmlUtilities.setCurrentDate()+".html");
			cJTxFdAddress.setText(sNewUrl);
			sUrlFile= sNewUrl;
			sUrlDisplayed= sNewUrl;
			createTocHtml(sHtml);
		}
	}



	/**
	 * Opens a Directory-structure with html files. Creates the ToC and
	 * the term-index.
	 *
	 * @modified 2010.05.10
	 * @since 2010.05.10 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ActionFileOpenDir extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionFileOpenDir() {
			super("hm.actFOpenDir");
		}

		public void actionPerformed(ActionEvent evt) {
			JFileChooser jFC = new JFileChooser(new File(getDirHome()));
			jFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int retval = jFC.showOpenDialog(cJFrHtmlMgr);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = jFC.getSelectedFile();
				if (file.isDirectory()){
					//1. create ToC
					createTocDir(file.toString());
				}
			}
		}
	}



	/**
	 * Opens an html-file OR an XmlToc.
	 *
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ActionFileOpenFile extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionFileOpenFile() {
			super("hm.actFOpenFile");
		}

		public void actionPerformed(ActionEvent evt) {
			FileDialog fileDialog = new FileDialog(cJFrHtmlMgr, getDirHome());
			fileDialog.setMode(FileDialog.LOAD);
			fileDialog.setVisible(true);
			String file = fileDialog.getFile();
			if (file ==	null)	{
				return;
			}
			File f = new File(fileDialog.getDirectory(), file);
			if (f.exists())	{
				String sFile= f.getAbsolutePath();
				String sUrl= HtmlUtilities.createUrlString(sFile);
				if (sFile.endsWith(".xml"))
					createTocXml(sUrl);
				else
					createTocFile(sUrl);
			}
		}
	}



	/**
	 * Opens an SFI-file from the WWW.
	 *
	 * @modified 2010.08.21
	 * @since 2010.08.21 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionFileOpenUrl extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionFileOpenUrl() {
			super("hm.actFOpenUrl");
		}

		public void actionPerformed(ActionEvent evt) {
			String sInput = JOptionPane.showInputDialog(
				getProperty("hm.txt.EnterUrl"), //message
				"http://htmlmgr.sourceforge.net/index.html"); //initial
			if(sInput != null) {
				displayUrl(sInput, true);
			}
		}
	}



	/**
	 *
	 * @modified 2010.07.07
	 * @since 2010.07.07 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionFileSave extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionFileSave() {
			super("hm.actFSave");
		}

		public void actionPerformed(ActionEvent evt) {
			//if window-title (modified), remove it
			String sTitle= cJFrHtmlMgr.getTitle();
			if (sTitle.indexOf(" (modified-")!=-1) {
				String sNT= sTitle.substring(0,sTitle.indexOf(" (modified-"));
				cJFrHtmlMgr.setTitle(sNT);
			}

			String sSFIw=""; //SFI with #
			if (sUrlDisplayed.indexOf("#")!=-1)
				sSFIw= sUrlDisplayed.substring(sUrlDisplayed.indexOf("#"));//with #

			FileDialog flDialog = new FileDialog(cJFrHtmlMgr);
			flDialog.setMode(FileDialog.SAVE);
//			flDialog.setDirectory(getDirHome());
			String file = getFileName(sUrlFile);
			//set date on file-name
			if (file.matches(".*-\\d\\d\\d\\d\\.\\d\\d\\.\\d\\d\\.htm.*"))
				file= file.substring(0,file.lastIndexOf("-"))
						+"-"+HtmlUtilities.setCurrentDate()+".html";
			else
				file= file.substring(0,file.lastIndexOf("."))
						+"-"+HtmlUtilities.setCurrentDate()+".html";
			flDialog.setFile(file);
			flDialog.setVisible(true);
			file = flDialog.getFile();	 //user input
			if (file == null) {			return;				}
			//create another file, without date.
			String file2= file.substring(0,file.lastIndexOf("-"))+".html";
			File f = new File(flDialog.getDirectory(), file);
			File f2 = new File(flDialog.getDirectory(), file2);
			try {
				FileOutputStream fos = new FileOutputStream(f);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
				FileOutputStream fos2 = new FileOutputStream(f2);
				OutputStreamWriter osw2 = new OutputStreamWriter(fos2, "UTF8");
				//save from lastMod
				String sHtml= "";
				if (sTitle.indexOf("modified-WysiWyg")!=-1) {
					sHtml= getSfiHtml(cEdWw);
//					bModified1=false;
				}
				else if (sTitle.indexOf("modified-Source")!=-1){
					sHtml= cEdSrc.getText();
//					bModified2= false;
				}
				else
					sHtml= getSfiHtml(cBrsr);

				//on sfi-files: set date
				if (sHtml.indexOf("\n<p class=\"last\">\n  VERSIONS")!= -1) {
					String sH1= sHtml.substring(0,sHtml.indexOf("\n<p class=\"last\">\n  VERSIONS"));
					String sH2= sHtml.substring(sHtml.indexOf("\n<p class=\"last\">\n  VERSIONS"));
					sH2= HtmlUtilities.replaceFirst(sH2,
								"\n  <br/>\\* CURRENT: (.*)-\\d\\d\\d\\d\\.\\d\\d\\.\\d\\d(.*)",
								"\n  <br/>* CURRENT: $1-"+HtmlUtilities.setCurrentDate()+"$2");
					sHtml= sH1+sH2;
				}

				osw.write(sHtml);
				osw.close();
				osw2.write(sHtml);
				osw2.close();
			} catch (Exception e) {
				System.out.println(e.toString());
			}

			String sNewUrl= HtmlUtilities.createUrlString(f.getAbsolutePath());
			//create new toc, because a modified file, may have new structure.
			createTocFile(sNewUrl);
			cJTxFdAddress.setText(sNewUrl+sSFIw);
			gotoFIinAddress();
//			displayUrl(sNewUrl +sSFIw);
		}
	}



	/**
	 *
	 * @modified 2010.09.12
	 * @since 2010.07.09 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionFileSaveAs extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionFileSaveAs() {
			super("hm.actFSaveAs");
		}

		public void actionPerformed(ActionEvent evt) {
			//if window-title (modified), remove it
			String sTitle= cJFrHtmlMgr.getTitle();
			if (sTitle.indexOf(" (modified-")!=-1) {
				String sNT= sTitle.substring(0,sTitle.indexOf(" (modified-"));
				cJFrHtmlMgr.setTitle(sNT);
			}

			String sSFIw=""; //SFI with #
			if (sUrlDisplayed.indexOf("#")!=-1)
				sSFIw= sUrlDisplayed.substring(sUrlDisplayed.indexOf("#"));//with #

			FileDialog flDialog = new FileDialog(cJFrHtmlMgr);
			flDialog.setMode(FileDialog.SAVE);
			flDialog.setTitle(getProperty("hm.txt.SaveAs"));
			String file = getFileName(sUrlFile);
			//set date on file-name
			if (file.matches(".*-\\d\\d\\d\\d\\.\\d\\d\\.\\d\\d\\.htm.*"))
				file= file.substring(0,file.lastIndexOf("-"))
						+"-"+HtmlUtilities.setCurrentDate()+".html";
			else
				file= file.substring(0,file.lastIndexOf("."))
						+"-"+HtmlUtilities.setCurrentDate()+".html";
			flDialog.setFile(file);
			flDialog.setVisible(true);
			file = flDialog.getFile();	 //user input
			if (file == null) {			return;				}
			//create another file, without date.
			String file2= file.substring(0,file.lastIndexOf("-"))+".html";
			File f = new File(flDialog.getDirectory(), file);
			File f2 = new File(flDialog.getDirectory(), file2);
			try {
				FileOutputStream fos = new FileOutputStream(f);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
				FileOutputStream fos2 = new FileOutputStream(f2);
				OutputStreamWriter osw2 = new OutputStreamWriter(fos2, "UTF8");
				//save from lastMod
				String sHtml= "";
				if (sTitle.indexOf("modified-WysiWyg")!=-1) {
					sHtml= getSfiHtml(cEdWw);
//					bModified1=false;
				}
				else if (sTitle.indexOf("modified-Source")!=-1){
					sHtml= cEdSrc.getText();
//					bModified2= false;
				}
				else
					sHtml= getSfiHtml(cBrsr);

				//on sfi-files: set date
				if (sHtml.indexOf("\n<p class=\"last\">\n  VERSIONS")!= -1) {
					String sH1= sHtml.substring(0,sHtml.indexOf("\n<p class=\"last\">\n  VERSIONS"));
					String sH2= sHtml.substring(sHtml.indexOf("\n<p class=\"last\">\n  VERSIONS"));
					sH2= HtmlUtilities.replaceFirst(sH2,
								"\n  <br/>\\* CURRENT: (.*)-\\d\\d\\d\\d\\.\\d\\d\\.\\d\\d\\.htm",
								"\n  <br/>* CURRENT: $1-2010.09.12.htm");
					sHtml= sH1+sH2;
				}

				osw.write(sHtml);
				osw.close();
				osw2.write(sHtml);
				osw2.close();
			} catch (Exception e) {
				System.out.println(e.toString());
			}

			String sNewUrl= HtmlUtilities.createUrlString(f.getAbsolutePath());
			//create new toc, because a modified file, may have new structure.
			createTocFile(sNewUrl);
			cJTxFdAddress.setText(sNewUrl+sSFIw);
			gotoFIinAddress();
//			displayUrl(sNewUrl +sSFIw);
		}
	}



	/**
	 * Closes the frame of the application.
	 *
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ActionFrameClose extends WindowAdapter
	{
		static final long serialVersionUID = 21L;
		public void windowClosing(WindowEvent e) {
			System.exit(0);// iii
//			cJFrHtmlMgr.dispose();
		}
	}



	/**
	 *
	 * @modified 2010.04.30
	 * @since 2010.04.30 (v00.02.02)
	 * @author Dirk Moebius
	 */
	class ActionGotoBack extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionGotoBack() {
			super("hm.actGBack");
		}

		public void actionPerformed(ActionEvent evt)
		{
			goBack();
		}
	}


	/**
	 * Handles the action on back|next buttons.
	 *
	 * @modified 2010.04.28
	 * @since 2010.04.28 (v00.02.02)
	 * @author Slava Pestov
	 */
/*
	class ActionButtonHistory extends AbstractAction //implements ActionListener does not show greek-letters
	{
		static final long serialVersionUID = 21L;

		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			String actionCommand = evt.getActionCommand();
//System.out.println(">>>ActionCommand= "+actionCommand);
			int separatorPosition = actionCommand.lastIndexOf(':');
			String url;
			int scrollPosition;
			if (-1 == separatorPosition)
			{
				url = actionCommand;
				scrollPosition = 0;
			}
			else {
				url = actionCommand.substring(0,separatorPosition);
				scrollPosition = Integer.parseInt(actionCommand.substring(separatorPosition+1));
			}
			if (url.length() != 0)
			{
				displayUrl(url,false,scrollPosition);
				return;
			}

			if(source == back)
			{
				HistoryEntry entry = historyModel.back(HtmlMgr.this);
				if(entry == null)
				{
					getToolkit().beep();
				}
				else {
					displayUrl(entry.url,false,entry.scrollPosition);
				}
			}
			else if(source == forward)
			{
				HistoryEntry entry = historyModel.forward(HtmlMgr.this);
				if(entry == null)
				{
					getToolkit().beep();
				}
				else {
					displayUrl(entry.url,false,entry.scrollPosition);
				}
			}
		}
	}
*/



	/**
	 *
	 * @modified 2010.09.13
	 * @since 2010.09.13 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionGotoFind extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionGotoFind()
		{
			super("hm.actGFind");
		}

		public void actionPerformed(ActionEvent evt)
		{
			cJTbPnL.setSelectedIndex(1);
			cSrchJTxFd.requestFocus();
		}
	}



	/**
	 *
	 * @modified 2010.04.30
	 * @since 2010.04.30 (v00.02.02)
	 * @author Dirk Moebius
	 */
	class ActionGotoForward extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionGotoForward() {
			super("hm.actGForward");
		}

		public void actionPerformed(ActionEvent evt) {
			goForward();
		}
	}



	/**
	 * Goes to Home-directory (http://htmlmgr.sourceforge.net/).
	 *
	 * @modified 2010.08.22
	 * @since 2010.04.30 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ActionGotoHome extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionGotoHome() {
			super("hm.actGHome");
		}

		public void actionPerformed(ActionEvent evt) {
			cJLbStatus.setText("... http://htmlmgr.sourceforge.net/");
			displayUrl("http://htmlmgr.sourceforge.net/", true);
		}
	}



	/**
	 * List the contents of a directory
	 * @modified 2010.05.02
	 * @since 2010.05.02 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ActionGotoListDirectory extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionGotoListDirectory()
		{
			super("hm.actGListDir");
		}

		public void actionPerformed(ActionEvent evt)
		{
			FileDialog fileDialog = new FileDialog(cJFrHtmlMgr);
			fileDialog.setMode(FileDialog.LOAD);
			fileDialog.setDirectory(getDirHome());
			fileDialog.setVisible(true);
			String stDir = fileDialog.getDirectory() ;
			if (stDir ==	null)	{
				return;
			}
			listDirectory(stDir);
		}
	}



	/**
	 *
	 * @modified 2010.04.30
	 * @since 2010.04.30 (v00.02.02)
	 * @author Dirk Moebius
	 */
	class ActionGotoReload extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionGotoReload() {
			super("hm.actGReload");
		}

		public void actionPerformed(ActionEvent evt)
		{
			reloadUrl();
		}
	}



	/**
	 * Displays Info about the HtmlMgr program.
	 *
	 * @modified 2010.06.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author Slava Pestov
	 */
	class ActionHelpAbout extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionHelpAbout() {
			super("hm.actHAbout");
		}

		public void actionPerformed(ActionEvent evt)
		{
			HtmlUtilities.message(getViewer(evt), "hm.dialogAbout",
				new String[] {sVersion});

		}
	}


	/**
	 * Shows the element-structure of the selected JEditoPane.
	 *
	 * @modified 2010.08.10
	 * @since 2010.08.01 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionViewHtmlJEditorPaneStructure extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionViewHtmlJEditorPaneStructure() {
			super("hm.actVJEPStructure");
		}

		public void actionPerformed(ActionEvent evt) {
				if (cJTbPnR.getSelectedIndex()==0)
					jEdPnStructure= new HtmlJEditorPaneStructure(cBrsr);
				else if (cJTbPnR.getSelectedIndex()==1)
					jEdPnStructure= new HtmlJEditorPaneStructure(cEdWw);
		}
	}

	/**
	 * Toggles wrapLines-property in JTextArea of source-editor.
	 *
	 * @modified 2010.07.04
	 * @since 2010.07.04 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ActionViewWrap extends HtmlMgrAction
	{
		static final long serialVersionUID = 21L;
		public ActionViewWrap() {
			super("hm.actVWrap");
		}

		public void actionPerformed(ActionEvent evt) {
			if (actVWrap.isSelected())
				cEdSrc.setLineWrap(true);
			else
				cEdSrc.setLineWrap(false);
			cEdSrc.repaint();
		}
	}

	/**
	 *
	 * @modified 2010.05.16
	 * @since 2010.05.16 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ClassTOCCellRenderer extends DefaultTreeCellRenderer
	{
		static final long serialVersionUID = 21L;
		EmptyBorder border = new EmptyBorder(1,0,1,1);

		public Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean focus)
		{
			super.getTreeCellRendererComponent(tree,value,sel,
				expanded,leaf,row,focus);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			Object obj = node.getUserObject();
			if(obj instanceof String){
				String sUrl= (String)obj;
				if (sUrl.startsWith("dir::")){
					setIcon(expanded ? iconDirOpen : iconDir);
					setText(phUrlTitle.get(sUrl));
				}
				else {
//System.out.println(sUrl); it is AMAZING the output of this variable!!!
					String sTitle= phUrlTitle.get(sUrl);
					if (sTitle!=null && sTitle.startsWith("#")){
						setIcon(iconParagraph);
						setText(sTitle);
					}
					else {
						if (sUrl.indexOf("#")== -1)
							setIcon(iconH0);
						else {
							sUrl= sUrl.substring(sUrl.indexOf("#")+1);
							int niP= HtmlUtilities.findHeadingLevel(sUrl);
							if (niP==1)
								setIcon(iconH1);
							else if (niP==2)
								setIcon(iconH2);
							else if (niP==3)
								setIcon(iconH3);
							else if (niP==4)
								setIcon(iconH4);
							else if (niP==5)
								setIcon(iconH5);
							else if (niP==6)
								setIcon(iconH6);
							else if (niP==7)
								setIcon(iconH7);
							else if (niP==8)
								setIcon(iconH8);
							else if (niP==9)
								setIcon(iconH9);
							else
								setIcon(iconH);
						}
						setText(sTitle);
					}
				}
			}
			setBorder(border);
			return this;
		}
	}



	/**
	 *
	 * @modified 2010.05.16
	 * @since 2010.05.16 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ClassTOCTree extends JTree
	{
		static final long serialVersionUID = 21L;

		ClassTOCTree()
		{
			ToolTipManager.sharedInstance().registerComponent(this);
			selectionModel.setSelectionMode(SINGLE_TREE_SELECTION);
		}

		public final String getToolTipText(MouseEvent evt)
		{
			TreePath path = getPathForLocation(evt.getX(), evt.getY());
			if(path != null)
			{
				Rectangle cellRect = getPathBounds(path);
				if(cellRect != null && !cellRectIsVisible(cellRect)){
					return phUrlTitle.get(path.getLastPathComponent().toString());
				}
			}
			return null;
		}

		public void processKeyEvent(KeyEvent evt)
		{
			if ((KeyEvent.KEY_PRESSED == evt.getID()) &&
					(KeyEvent.VK_ENTER == evt.getKeyCode()))
			{
				TreePath path = getSelectionPath();
				if(path != null)
				{
					Object obj = ((DefaultMutableTreeNode)
						path.getLastPathComponent())
						.getUserObject();
					if(!(obj instanceof String))
					{
						this.expandPath(path);
						return;
					}
					displayUrl((String)obj, true);
				}
				evt.consume();
			}
			else {
				super.processKeyEvent(evt);
			}
		}

		protected void processMouseEvent(MouseEvent evt)
		{
			//ToolTipManager ttm = ToolTipManager.sharedInstance();

			switch(evt.getID())
			{
				case MouseEvent.MOUSE_CLICKED:
					TreePath path = getPathForLocation(evt.getX(),evt.getY());
					if(path != null)
					{
						if(!isPathSelected(path))
							setSelectionPath(path);

						Object obj = ((DefaultMutableTreeNode)
							path.getLastPathComponent()).getUserObject();
						if(!(obj instanceof String))
						{
							this.expandPath(path);
							return;
						}
						displayUrl((String)obj, true);
					}

					super.processMouseEvent(evt);
					break;
				default:
					super.processMouseEvent(evt);
					break;
			}
		}

		private boolean cellRectIsVisible(Rectangle cellRect)
		{
			Rectangle vr = ClassTOCTree.this.getVisibleRect();
			return vr.contains(cellRect.x,cellRect.y) &&
				vr.contains(cellRect.x + cellRect.width,
				cellRect.y + cellRect.height);
		}
	}


	/**
	 *
	 * @modified 2010.05.20
	 * @since 2010.05.20 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ClassToCXmlHandler extends DefaultHandler
	{
		public String									sUrlDisplay;

		private String									sTitle;
		private String 								sUrl;
		private DefaultMutableTreeNode trndParent;
		private Stack<DefaultMutableTreeNode> stckNodes;


		ClassToCXmlHandler()
		{
			stckNodes = new Stack<DefaultMutableTreeNode>();
		}

		public void startElement(String uri, String localName,
														String strElName, Attributes attrs)
		{
			if (strElName.equals("trNode")){
				sTitle = attrs.getValue("title");
				sUrl = attrs.getValue("url");
				if (!sUrl.startsWith("dir::")){
					File currentDir = new File(System.getProperty("user.dir"));
					File previousDir = currentDir.getParentFile();
					String sDirPrev = previousDir.getAbsolutePath() +"/";
					sUrl = HtmlUtilities.createUrlString(sDirPrev + sUrl);
				}
				if (attrs.getValue("display")!=null
						&& attrs.getValue("display").equals("y"))
					sUrlDisplay= sUrl;

				DefaultMutableTreeNode newNode = createTocNode(sUrl);
				storeTitle(sUrl, sTitle);

				if (trndParent == null) {//the first-time
					trndParent= newNode;//always a dir-node
					cTocNdRoot= newNode;
					stckNodes.push(newNode);
				} else {
					trndParent= stckNodes.peek();
					if (!sUrl.startsWith("dir::")) {
						if (HtmlUtilities.isSFIFile(sUrl)) {
							lvUrlFilesToc.add(sUrl);
							ltsUrlFilesVisited.add(sUrl);
							new HtmlParser(sUrl, null, trndParent);
						}
						else {
							JOptionPane.showMessageDialog(null,
								"HtmlParser: NOT SFI-file: "+sUrl);
							openBrowser(sUrl);
							return;
						}

					} else
						trndParent.add(newNode);

					stckNodes.push(newNode);
				}
			}
		}

		public void endElement(String uri, String localName, String strElName)
		{
			if(strElName == null)
				return;
			if(strElName.equals("trNode"))
				trndParent = stckNodes.pop();
		}
	}



	/**
	 * Synchronize the selected-TxtComponent-tab and
	 * the cursor goes at the SFI in the address-bar.
	 *
	 * @modified 2010.08.09
	 * @since 2010.07.01 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ListenerChangeTabR implements ChangeListener
	{
		public void stateChanged(ChangeEvent e) {
			JTabbedPane cTabR = (JTabbedPane) e.getSource();
			synchronizeTabR(cTabR.getSelectedIndex());
			gotoFIinAddress();
		}
	}



	/**
	 * Detects changes in documents of editors, and sets the corresponding
	 * booleans as true.
	 *
	 * @modified 2010.07.27
	 * @since 2010.07.01 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ListenerDocument implements DocumentListener
	{
		Document doc;
		public void insertUpdate(DocumentEvent e) {
			doc = e.getDocument();
			if (doc instanceof HTMLDocument) {
				bModified1=true;
				setWindowTitleModified(1);
//				System.out.println("edWW-inserted");
			}
			else if (doc instanceof PlainDocument) {
				bModified2=true;
				//if <title> changed, change title
				PlainDocument docP= (PlainDocument)doc;
				Element elem= docP.getParagraphElement(cEdSrc.getCaretPosition());
				String sETxt= getTextOfElement(docP,elem);
				String sTitle="";
				if (sETxt.indexOf("<title>")!=-1) {
					sTitle= sETxt.substring(sETxt.indexOf(">")+1,
									sETxt.lastIndexOf("<"));
					cBrsr.getDocument().putProperty("title", sTitle);
					cEdWw.getDocument().putProperty("title", sTitle);
					setWindowTitle(sTitle+" (modified-Source)");
				}
				else
					setWindowTitleModified(2);
//				System.out.println("EdSrc-inserted");
			}
		}

		public void removeUpdate(DocumentEvent e) {
			doc = e.getDocument();
			if (doc instanceof HTMLDocument) {
				bModified1=true;
				setWindowTitleModified(1);
//				System.out.println("EdWW-removed");
			}
			else if (doc instanceof PlainDocument) {
				bModified2=true;
				//if <title> changed, change title
				PlainDocument docP= (PlainDocument)doc;
				Element elem= docP.getParagraphElement(cEdSrc.getCaretPosition());
				String sETxt= getTextOfElement(docP,elem);
				String sTitle="";
				if (sETxt.indexOf("<title>")!=-1) {
					sTitle= sETxt.substring(sETxt.indexOf(">")+1,
									sETxt.lastIndexOf("<"));
					cBrsr.getDocument().putProperty("title", sTitle);
					cEdWw.getDocument().putProperty("title", sTitle);
					setWindowTitle(sTitle+" - HtmlMgr (modified-Source)");
				}
				else
					setWindowTitleModified(2);
//				System.out.println("EdSrc-removed");
			}
		}

		public void changedUpdate(DocumentEvent e) {
			//style changes.
			doc = e.getDocument();
			if (doc instanceof HTMLDocument) {
				bModified1=true;
				setWindowTitleModified(1);
				System.out.println("EdWW-StyleChanged");
			}
		}
	}



	/**
	 * Performs the search-action of tab-search.<br/>
	 * First search for the terms (= tokens separated by space).<br/>
	 * Then search for the words of the terms (= tokens separated by "-_'").
	 *
	 * @modified 2010.05.25
	 * @since 2010.05.25 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ListenerActionSearch implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			//1) search for the terms
			//2) search for words.
			final String txtSearch = cSrchJTxFd.getText();
			niOccurance= 0;
			lvFilesFound= new Vector<String>();
			cSrchNdRoot= new DefaultMutableTreeNode(txtSearch, true);//allowsChildren if specified

			//Tokenize the search-phrase, into TERMS.
			StringTokenizer st = new StringTokenizer(txtSearch," ");
			int intTkns= st.	countTokens();
			//Create a llist to hold the terms, which then will tokenize into words.
			LinkedList<String> llTerms = new LinkedList<String>();

			while(st.hasMoreTokens())
			{
				String strTrm = st.nextToken().toLowerCase();
				//put on llTerms ONLY if contain words:
				if (strTrm.indexOf("-")!=-1||strTrm.indexOf("_")!=-1||strTrm.indexOf("'")!=-1)
					llTerms.add(strTrm);
				if (tvWordIgnored.contains(strTrm))
					continue;
				addTrNodesOnSearchTree(strTrm, intTkns);
			}

			//Tokenize the TERMS, into WORDS ppp
			for(int i=0; i<llTerms.size(); i++)
			{
				String strTrm = llTerms.remove();
				StringTokenizer st2 = new StringTokenizer(strTrm,"-_'");
				while(st2.hasMoreTokens())
				{
					String strWrd = st2.nextToken().toLowerCase();
					if (tvWordIgnored.contains(strWrd))
						continue;
					addTrNodesOnSearchTree(strWrd, 2);//to be here, we first searched for a term
				}
			}

			cSrchTrMdl = new DefaultTreeModel(cSrchNdRoot);
			//put occurances after root for visibility.
			DefaultMutableTreeNode trndOccurances= new DefaultMutableTreeNode(
				">> " +niOccurance+" occurances in " +lvFilesFound.size()+" files");
			cSrchTrMdl.insertNodeInto(trndOccurances, cSrchNdRoot, 0);
			cSrchTrMdl.reload(cSrchNdRoot);
			cSrchJTr.setModel(cSrchTrMdl);
			cSrchJTr.setRootVisible(true);
		}
	}


	/**
	 *
	 * @modified 2010.05.01
	 * @since 2010.05.01 (v00.02.02)
	 * @author Dirk Moebius
	 */
	class ListenerActionURLButton implements ActionListener
	{
		private boolean bAddToHistory = true;

		public ListenerActionURLButton(boolean bAddToHistory)
		{
			this.bAddToHistory = bAddToHistory;
		}

		/**
		 * A bookmark was selected in the Bookmarks menu. Open the
		 * corresponding URL in the InfoViewer. The URL will be added to
		 * the history, if this URLButtonHandler was initialized with
		 * <code>bAddToHistory = true</code>.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			String cmd = evt.getActionCommand();
			displayUrl(cmd, bAddToHistory);
		}
	}



	/**
	 * Handles Keys.
	 *
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author Slava Pestov
	 */
	class ListenerKey extends KeyAdapter
	{
		public void keyPressed(KeyEvent ke)
		{
			switch (ke.getKeyCode())
			{
				//ppp choose scrollPane on selected-tab.
				case KeyEvent.VK_UP:
					JScrollBar scrollBar = cJScrPnBrsr.getVerticalScrollBar();
					scrollBar.setValue(scrollBar.getValue()-scrollBar.getUnitIncrement(-1));
					ke.consume();
					break;
				case KeyEvent.VK_DOWN:
					scrollBar = cJScrPnBrsr.getVerticalScrollBar();
					scrollBar.setValue(scrollBar.getValue()+scrollBar.getUnitIncrement(1));
					ke.consume();
					break;
				case KeyEvent.VK_LEFT:
					scrollBar = cJScrPnBrsr.getHorizontalScrollBar();
					scrollBar.setValue(scrollBar.getValue()-scrollBar.getUnitIncrement(-1));
					ke.consume();
					break;
				case KeyEvent.VK_RIGHT:
					scrollBar = cJScrPnBrsr.getHorizontalScrollBar();
					scrollBar.setValue(scrollBar.getValue()+scrollBar.getUnitIncrement(1));
					ke.consume();
					break;
	//			case KeyEvent.VK_ESCAPE:
	//				dismiss();//dismiss docable window
	//				ke.consume();
	//				break;
			}
		}
	}



	/**
	 * Gets the SFI in the text.<br/>
	 * Finds the location of that position.<br/>
	 * Selects in the ToC the node with that location.
	 *
	 * @modified 2010.04.30
	 * @since 2010.04.30 (v00.02.02)
	 * @author Dirk Moebius
	 */
	class ListenerMouseLocator extends MouseAdapter
	{
		JPopupMenu cJPopumMn = null;

		public void mousePressed(MouseEvent evt)
		{
//			if ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			if (evt.getButton()== MouseEvent.BUTTON1 ) {
				evt.consume();

				AccessibleText aTxt= cBrsr.getAccessibleContext().getAccessibleText();
				int niP= -1;
				HTMLDocument hDoc= new HTMLDocument();
				String sElTxt="";
				String sUrlDoc="";
				String sOffsets="";

				if (cJTbPnR.getSelectedIndex()==2){
					aTxt = cEdSrc.getAccessibleContext().getAccessibleText();
					niP = aTxt.getIndexAtPoint(evt.getPoint());
					PlainDocument docPl= (PlainDocument)cEdSrc.getDocument();
					Element eRoot= docPl.getDefaultRootElement();
					int niIdxCh= eRoot.getElementIndex(niP);
					sElTxt="a name=\"h0p1\"";
					for (int i=niIdxCh; i>0; i--){
						//finds the PREVIOUS element with SFI
						Element eP= eRoot.getElement(i);
						String sTx= getTextOfElement(docPl,eP);
						if (sTx.indexOf("name=\"h") != -1){
							sElTxt= sTx;
							break;
						}
					}
					String sSFI= sElTxt.substring(sElTxt.indexOf("\"")+1,
														sElTxt.indexOf("\"",sElTxt.indexOf("\"")+1));
					sUrlDoc= sUrlFile +"#" +sSFI;
//System.out.println("..el-clicked: " +sSFI);
				}
				else {
					if (cJTbPnR.getSelectedIndex()==0){
						aTxt = cBrsr.getAccessibleContext().getAccessibleText();
						niP = aTxt.getIndexAtPoint(evt.getPoint());
						hDoc= (HTMLDocument)cBrsr.getDocument();
					}
					else if (cJTbPnR.getSelectedIndex()==1){
						aTxt = cEdWw.getAccessibleContext().getAccessibleText();
	//				Integer ingPos= Integer.valueOf(txt.getIndexAtPoint(evt.getPoint()));
	//				System.out.println(ingPos);
						niP = aTxt.getIndexAtPoint(evt.getPoint());
						hDoc= (HTMLDocument)cEdWw.getDocument();
					}
					Element elem= hDoc.getParagraphElement(niP);
					sOffsets= elem.getStartOffset()+","+elem.getEndOffset();
					sElTxt= getTextOfElement(hDoc, elem);
//System.out.println(sElTxt);
					//if a list has no SFI, we have an exception
					if (sElTxt.indexOf("#h") != -1){
						String txtSFI= sElTxt.substring(sElTxt.indexOf("#h")+1,
																	sElTxt.indexOf("#",sElTxt.indexOf("#h")+1));
						if (txtSFI.indexOf("-hd") != -1)
							txtSFI= txtSFI.substring(0,txtSFI.indexOf("-hd"));
						sUrlDoc= sUrlFile +"#" +txtSFI;
					}
					else {
						sUrlDoc= sUrlFile;
					}
				}

//Element child = e.getElement(0);
//AttributeSet chAtt = child.getAttributes();
//String strNmValue = (String)chAtt.getAttribute("name");
//System.out.println(strNmValue);
//						if (chAtt.getAttribute(StyleConstants.NameAttribute) ==
//								HTML.Tag.CONTENT) {
//System.out.println(child.toString());
//System.out.println(e.toString());
				//1) get the pair <pos,location> for currentfile.
//				TreeMap<Integer,String> tpPos= pHUrlFilePosTM.get(sUrlFile);
//				Integer ingLT= tpPos.floorKey(ingPos);
//				String sUrlDoc= tpPos.get(ingLT);
//System.out.println("..lastPos: "+tpPos.lastKey());
//System.out.println("..getPosOff: "+hDoc.getEndPosition().getOffset());
//System.out.println("..getLengthChar: "+hDoc.getLength());
//System.out.println("..lessthan: "+ingLT+": "+sUrlDoc);

				//2) finde the node in tree.
				selectTreeNode(sUrlDoc);
				//select the tab with toc
				cJTbPnL.setSelectedIndex(0);
				if (sUrlDisplayed!=null)
					if (sUrlDisplayed.indexOf(":")!=-1)
						historyBF.add(sUrlDisplayed);
				// reset default cursor so that the hand cursor doesn't stick around
				cEdWw.setCursor(Cursor.getDefaultCursor());
				cJTxFdAddress.setText(sUrlDoc);
				sUrlDisplayed= sUrlDoc;
				updateBFButtons();
				updateGotoMenu();

				//3) select the right row in HtmlJEditorPaneStructure
				//from browser or editor-ww
				if (jEdPnStructure != null && !sOffsets.equals("")){
					jEdPnStructure.selectRow(sOffsets);
				}
			}

			else if (evt.getButton()== MouseEvent.BUTTON3 ) {
				evt.consume();
				if (cJTbPnR.getSelectedIndex()==0){
					//always display the url on address
					System.out.println("popup-browser");
					cJPopumMn= new JPopupMenu();
					cJPopumMn.add(actGBack);
					cJPopumMn.add(actGForward);
					cJPopumMn.addSeparator();
					cJPopumMn.add(actGReload);
					cJPopumMn.show(cBrsr, evt.getX() - 1, evt.getY() - 1);
				}
				else if (cJTbPnR.getSelectedIndex()==1) {
					System.out.println("ww-editor");
					cJPopumMn= new JPopupMenu();
					cJPopumMn.add(actGBack);
					cJPopumMn.add(actGForward);
					cJPopumMn.addSeparator();
					cJPopumMn.add(actGReload);
					cJPopumMn.show(cEdWw, evt.getX() - 1, evt.getY() - 1);
				}
				else if (cJTbPnR.getSelectedIndex()==2) {
					cJPopumMn= new JPopupMenu();
					cJPopumMn.add(actGBack);
					cJPopumMn.add(actGForward);
					cJPopumMn.add(actGReload);
					cJPopumMn.addSeparator();
					JMenuItem jMItem = actVWrap.menuItem();
					cJPopumMn.add(jMItem);
					cJPopumMn.show(cEdSrc, evt.getX() - 1, evt.getY() - 1);
				}
			}
		}
	}


	/**
	 * Displays the SFI we are clicking on the tree of tab-search.
	 *
	 * @modified 2010.08.21
	 * @since 2010.05.25 (v00.02.02)
	 * @author HoKoNoUmo
	 */
	class ListenerMouseSearch extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			String sUrl= "";
			TreePath path = cSrchJTr.getPathForLocation(e.getX(), e.getY());
			if(path != null){
				//#h0.1p1
				String sLstPath= path.getLastPathComponent().toString();
				//index.html#h0.1p1 (G:/FILE1/HtmlMgr/doc/)
				String sLstPthPrnt= path.getParentPath().getLastPathComponent().toString();

				if (sLstPath.startsWith("#")) {
					//other nodes in the tree, are not displayed.
					String sFile= sLstPthPrnt.substring(0,sLstPthPrnt.indexOf(" "));
					String sDir= sLstPthPrnt.substring(sLstPthPrnt.indexOf("(")+1,
																						sLstPthPrnt.indexOf(")"));
					if (sDir.startsWith("http"))
						sUrl= sDir+sFile+sLstPath;
					else
						sUrl= "file:"+sDir+sFile+sLstPath;
					displayUrl(sUrl);
					//
					String sFindTxt= path.getParentPath().getParentPath().getLastPathComponent().toString();
					findText(sFindTxt, false, findStartOffsetOfCurrentSFIElement());
				}
			}
		}
	}



	/**
	 * Handles Hyperlinks-events:<br/>
	 * 1) Activate Hyperlinks.<br/>
	 * 2) Display in status the link, when the mouse is over.
	 *
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author Slava Pestov
	 */
	class ListenerHyperlink implements HyperlinkListener
	{
		public void hyperlinkUpdate(HyperlinkEvent evt)
		{
			//if url.toString() STARTSWITH @QL:, we can implement here
			//a QUERY-LINK.
			URL url = evt.getURL();
			if(evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				if(evt instanceof HTMLFrameHyperlinkEvent) {
					((HTMLDocument)cEdWw.getDocument())
						.processHTMLFrameHyperlinkEvent(
						(HTMLFrameHyperlinkEvent)evt);
				} else {
					if(url != null) {
						displayUrl(url.toString(),true);
					}
				}
			}
			else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED)
			{
//				cEdWw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				if (url != null)
					cJLbStatus.setText(url.toString());
				updateBFButtons();
			}
			else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED)
			{
//				cEdWw.setCursor(Cursor.getDefaultCursor());
				cJLbStatus.setText("");
			}
		}
	}


	/**
	 *
	 *
	 * @modified 2010.04.29
	 * @since 2010.04.29 (v00.02.02)
	 * @author Slava Pestov
	 */
	class ListenerPropertyEdWw implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent evt)
		{
			cJPBarEdWw.setIndeterminate(false);
			cEdWw.getDocument().addDocumentListener(lsnDoc);
			cEdWw.getDocument().addUndoableEditListener(lsnUndo);

//			if("page".equals(evt.getPropertyName())) {
//				String sTitle = (String)cEdWw.getDocument()
//					.getProperty("title");
//				if(sTitle == null) {
//					sTitle = getFileName(cEdWw.getPage().toString());
//				}
//				setWindowTitle(sTitle); //only when we change file nnn
//				cJLbStatus2.setText(titleStr);
//				historyModel.updateTitle(cEdWw.getPage().toString(),
//					titleStr);
//			}
		}
	}



	/**
	 * Listener for undoableEdits.
	 *
	 * @modified 2010.08.11
	 * @since 2010.08.11 (v00.02.03)
	 * @author HoKoNoUmo
	 */
	class ListenerUndoableEdit implements UndoableEditListener
	{
		public void undoableEditHappened(UndoableEditEvent e) {
			undoMgr.addEdit(e.getEdit());
			actEUndo.update();
			actERedo.update();
		}
	}


}
