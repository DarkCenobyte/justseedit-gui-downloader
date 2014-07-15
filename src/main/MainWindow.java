package main;

import java.awt.EventQueue;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JFrame;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JLabel;
import javax.swing.JSplitPane;

import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ScrollPaneConstants;

import java.awt.Dimension;


public class MainWindow {

	private JFrame frmJustseeditGuiDownloader;
	private static String APIKey = null;
	private String infoHash = null;

	private final String userPathS = System.getProperty("user.home")+FileSystems.getDefault().getSeparator()+".justseedgui";
	private final String userFilePathS = System.getProperty("user.home")+FileSystems.getDefault().getSeparator()+".justseedgui"+FileSystems.getDefault().getSeparator()+"config.conf";
	private final Path userPath = Paths.get(userPathS);
	private final Path userFilePath = Paths.get(userFilePathS); 
	
	private final String RESTUri = "https://api.justseed.it";
	private String fullOutput = "";
	private String listURI = "";
	private Document document;
	
	private int torrentIndex = 0;
	
	private DefaultListModel<String> torrentListModel = new DefaultListModel<String>();
	private JList<String> list;
	private JLabel lblTname;
	private JLabel lblTsize;
	private JLabel lblTstatus;
	private JButton btnDownloadTo;
	private final ImageIcon abouticon = new ImageIcon(this.getClass().getResource("/res/icon/about.png"));
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmJustseeditGuiDownloader.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
		
		//check about first launch/conf file/...
		if(Files.exists(userFilePath)){
			try {
				APIKey = new String(Files.readAllBytes(userFilePath));
				if(APIKey.isEmpty()){
					System.out.println("WARNING: API Key file is empty...");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			//create default file (for the moment, contains only API key)	
			try {
				Files.createDirectories(userPath);
				Files.createFile(userFilePath);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new GetAPIKey();
			try {
				APIKey = new String(Files.readAllBytes(userFilePath));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		getTorrentList();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmJustseeditGuiDownloader = new JFrame();
		frmJustseeditGuiDownloader.setTitle("JustSeed.it GUI Downloader");
		frmJustseeditGuiDownloader.setBounds(100, 100, 640, 480);
		frmJustseeditGuiDownloader.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmJustseeditGuiDownloader.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//About the author :)
				JOptionPane about = new JOptionPane(
						"JustSeed.it GUI Downloader\n"
						+"version 1.0\n"
						+"by Seror-Droin Olivier (aka DarkCenobyte)\n"
						+"This is an unofficial tool, using public API of JustSeed.it.\n"
						+"Distributed under the MIT License", JOptionPane.INFORMATION_MESSAGE);
				about.setIcon(abouticon);
				JDialog dialog = about.createDialog("About");
				dialog.setVisible(true);
			}
		});
		mnFile.add(mntmAbout);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmJustseeditGuiDownloader.dispose();
			}
		});
		
		JMenuItem mntmRefreshList = new JMenuItem("Refresh list");
		mntmRefreshList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getTorrentList();
			}
		});
		mnFile.add(mntmRefreshList);
		mnFile.add(mntmExit);
		
		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);
		
		JMenuItem mntmSetApiKey = new JMenuItem("Set API Key");
		mntmSetApiKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new GetAPIKey();
				try {
					APIKey = new String(Files.readAllBytes(userFilePath));
					torrentListModel.clear();
					getTorrentList();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		mnSettings.add(mntmSetApiKey);
		frmJustseeditGuiDownloader.getContentPane().setLayout(new GridLayout(1, 0, 0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		frmJustseeditGuiDownloader.getContentPane().add(splitPane);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(180, 23));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		splitPane.setLeftComponent(scrollPane);
		
		list = new JList<String>();
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				selectTorrent(list.getSelectedIndex());
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
		
		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.PREF_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(128dlu;pref)"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		btnDownloadTo = new JButton("Download to...");
		btnDownloadTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//launch Downloader
				downloadTorrent(torrentIndex);
			}
		});
		btnDownloadTo.setEnabled(false);
		panel.add(btnDownloadTo, "2, 2");
		
		JLabel lblName = new JLabel("Name:");
		panel.add(lblName, "2, 6");
		
		lblTname = new JLabel("");
		panel.add(lblTname, "4, 6, fill, default");
		
		JLabel lblSize = new JLabel("Size:");
		panel.add(lblSize, "2, 8");
		
		lblTsize = new JLabel("");
		panel.add(lblTsize, "4, 8");
		
		JLabel lblStatus = new JLabel("Status:");
		panel.add(lblStatus, "2, 10");
		
		lblTstatus = new JLabel("");
		panel.add(lblTstatus, "4, 10");
	}
	public JList<String> getList() {
		return list;
	}
	public void getTorrentList(){
		//get torrent list:
		fullOutput = "";
		try {
			URL url = new URL(RESTUri+"/torrents/list.csp?api_key="+APIKey);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/xml");
			
			/*test server response*/
			//System.out.println("Response Server:"+conn.getResponseCode());
			//System.out.println(conn.getResponseMessage());
			
			//test contents
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output;
			
			while((output = br.readLine()) != null){
				fullOutput = fullOutput+output;
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//parse XML result:
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = parser.parse(new InputSource(new StringReader(fullOutput)));
			Element status = (Element)document.getElementsByTagName("status").item(0);
			String statusMessage = status.getTextContent();
			if(statusMessage == "FAILURE"){
				Element failureMessage = (Element)document.getElementsByTagName("message").item(0);
				System.out.println(failureMessage.getTextContent());
			}
			else
			{
				//parse torrent listing
				NodeList torrentRow = document.getElementsByTagName("row");
				for(int i=0;i<torrentRow.getLength();i++){
					//add torrent name to JList
					String torrentName = ((Element)torrentRow.item(i).getChildNodes()).getElementsByTagName("name").item(0).getTextContent();
					
					//convert %20 to space, and other special characters... (incomplete and need to be improve to support asians/... characters)
					torrentName = torrentName.replace("%20", " ");
					torrentName = torrentName.replace("%21", "!");
					torrentName = torrentName.replace("%22", "\"");
					torrentName = torrentName.replace("%23", "#");
					torrentName = torrentName.replace("%24", "$");
					torrentName = torrentName.replace("%25", "%");
					torrentName = torrentName.replace("%26", "&");
					torrentName = torrentName.replace("%27", "'");
					torrentName = torrentName.replace("%28", "(");
					torrentName = torrentName.replace("%29", ")");
					torrentName = torrentName.replace("%2a", "*");
					torrentName = torrentName.replace("%2b", "+");
					torrentName = torrentName.replace("%2c", ",");
					torrentName = torrentName.replace("%2d", "-");
					torrentName = torrentName.replace("%2e", ".");
					torrentName = torrentName.replace("%2f", "/");
					torrentName = torrentName.replace("%5b", "[");
					torrentName = torrentName.replace("%5c", "\\");
					torrentName = torrentName.replace("%5d", "]");
					torrentName = torrentName.replace("%5f", "_");
					
					torrentListModel.addElement(torrentName);
				}
				list.setModel(torrentListModel);
			}
 
 
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void selectTorrent(int selectedIndex){
		//update informations
		//System.out.println("You choose element: "+selectedIndex);
		if(selectedIndex!=-1) //fix an ArrayIndexOutOfBoundsException.
		{
			Element torrentSelected = (Element)document.getElementsByTagName("row").item(selectedIndex);
			String torrentPercent = torrentSelected.getElementsByTagName("percentage_as_decimal").item(0).getTextContent();
			String torrentStatus = torrentSelected.getElementsByTagName("status").item(0).getTextContent();
			String torrentName = torrentListModel.elementAt(selectedIndex); //get it from the list, don't need to convert characters again.
			String torrentSize = torrentSelected.getElementsByTagName("size_as_string").item(0).getTextContent();
			
			infoHash = torrentSelected.getElementsByTagName("info_hash").item(0).getTextContent();
			
			lblTname.setText(torrentName);
			lblTsize.setText(torrentSize);
			lblTstatus.setText("Completed: "+torrentPercent+" Actually: "+torrentStatus);
			
			//active buttons
			if(torrentPercent.contains("100")){
				torrentIndex = selectedIndex;
				btnDownloadTo.setEnabled(true);
			}
			else
			{
				btnDownloadTo.setEnabled(false);
			}
		}
	}
	public JLabel getLblTname() {
		return lblTname;
	}
	public JLabel getLblTsize() {
		return lblTsize;
	}
	public JLabel getLblTstatus() {
		return lblTstatus;
	}
	public JButton getBtnDownloadTo() {
		return btnDownloadTo;
	}
	public void downloadTorrent(int selectedIndex){
		//get download link
		listURI = "";
		String statusMessage = null;
		try {
			URL url = new URL(RESTUri+"/torrent/links/list.csp?api_key="+APIKey+"&info_hash="+infoHash);
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/xml");
			
			/*test server response*/
			//System.out.println("Response Server:"+conn.getResponseCode());
			//System.out.println(conn.getResponseMessage());
			
			//test contents
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output;
			
			while((output = br.readLine()) != null){
				listURI = listURI+output;
			}
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = parser.parse(new InputSource(new StringReader(listURI)));
			Element status = (Element)document.getElementsByTagName("status").item(0);
			statusMessage = status.getTextContent();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//check for dead link on the server:
		if(statusMessage.contains("FAILURE")){
			//Files probably need to be releech by the server.
			JOptionPane suggestReleech = new JOptionPane(
					"The links seems to be dead on the server,\n"
					+"You probably need to releech their.\n"
					+"Switch this torrent to \"Running\" state on the server?",
					JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
			JDialog dialog = suggestReleech.createDialog("Releech ?");
			dialog.setVisible(true);
			//System.out.println(suggestReleech.getValue()); //YES = 0, NO = 1
			if((int)suggestReleech.getValue() == 0){
				URL url;
				try {
					url = new URL(RESTUri+"/torrent/start.csp?api_key="+APIKey+"&info_hash="+infoHash);
					HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Accept", "application/xml");
					
					//test server response
					//System.out.println("Response Server:"+conn.getResponseCode());
					//System.out.println(conn.getResponseMessage());
					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				getTorrentList(); //fix a crash
			}
		}
		else{
			new Downloader(torrentListModel.elementAt(selectedIndex), document);
			getTorrentList(); //fix a crash
		}
	}
}
