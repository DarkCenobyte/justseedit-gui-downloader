package main;

import java.awt.Desktop;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JLabel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Downloader extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2106521530868389748L;

	private JPanel contentPane;
	
	private String torrentName = null;
	private Document URI = null;
	private int downloaded = 0;
	private int numURI = 0;
	private String rootFolder = null;
	private ArrayList<String> listURI = new ArrayList<String>();
	private JProgressBar progressBar;
	private JLabel lblTorrentname;
	private JLabel lblDltobedl;
	private JLabel lblCurfile;
	private FileOutputStream fos = null;
	private JLabel lblStatusm;
	private JButton btnClose;
	private JButton btnCancel;

	/**
	 * Create the frame.
	 */
	public Downloader(String tName, Document uri) {
		torrentName = tName;
		URI = uri;
		
		setTitle("Download");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(12, 12, 422, 142);
		contentPane.add(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(200dlu;default)"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblName = new JLabel("Name:");
		panel.add(lblName, "2, 2");
		
		lblTorrentname = new JLabel("");
		panel.add(lblTorrentname, "4, 2, fill, default");
		
		JLabel lblFilesDownloaded = new JLabel("File(s) Downloaded:");
		panel.add(lblFilesDownloaded, "2, 4");
		
		lblDltobedl = new JLabel("");
		panel.add(lblDltobedl, "4, 4, fill, default");
		
		JLabel lblCurrentFile = new JLabel("Current file:");
		panel.add(lblCurrentFile, "2, 6");
		
		lblCurfile = new JLabel("");
		panel.add(lblCurfile, "4, 6, fill, default");
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setBounds(12, 180, 422, 24);
		contentPane.add(progressBar);
		
		JButton btnOpenDirectory = new JButton("Open Directory");
		btnOpenDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//open rootfolder
				try {
					Desktop.getDesktop().open(new File(rootFolder));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnOpenDirectory.setBounds(210, 237, 134, 23);
		contentPane.add(btnOpenDirectory);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//cancel download
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dispose();
			}
		});
		btnCancel.setBounds(354, 237, 80, 23);
		contentPane.add(btnCancel);
		
		JProgressBar littleProgressBar = new JProgressBar();
		littleProgressBar.setVisible(false);
		littleProgressBar.setStringPainted(true);
		littleProgressBar.setBounds(12, 162, 422, 17);
		contentPane.add(littleProgressBar);
		setVisible(true);
		
		chooseRootFolder();
		parseURI();
		
		lblTorrentname.setText(torrentName);
		lblDltobedl.setText(downloaded+"/"+numURI);
		
		JLabel lblStatus = new JLabel("Status:");
		panel.add(lblStatus, "2, 8");
		
		lblStatusm = new JLabel("");
		panel.add(lblStatusm, "4, 8");
		
		numURI = listURI.size();
		//determine the maximum value for progressbar
		progressBar.setMaximum(numURI);
		progressBar.setValue(0);
		progressBar.setIndeterminate(false);
		
		btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//simply close the window
				dispose();
			}
		});
		btnClose.setEnabled(false);
		btnClose.setBounds(12, 237, 89, 23);
		contentPane.add(btnClose);
		lblStatusm.setText("Downloading...");
		download();
	}
	public void chooseRootFolder(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Select folder");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			//for testing purpose, need to change the end of this line to "toPath();".
			rootFolder = chooser.getSelectedFile().getAbsolutePath();
		}
		else{
			//cancel
			dispose();
		}
	}
	public void parseURI(){
		NodeList torrentRow = URI.getElementsByTagName("row");
		for(int i=0;i<torrentRow.getLength();i++){
			//add URI to a list
			String fileURI = ((Element)torrentRow.item(i).getChildNodes()).getElementsByTagName("url").item(0).getTextContent();
			//clean URI
			fileURI = fileURI.replace("%20", " ");
			fileURI = fileURI.replace("%3a", ":");
			fileURI = fileURI.replace("%252f", "/");
			fileURI = fileURI.replace("%2f", "/");
			//System.out.println(fileURI);
			listURI.add(fileURI);
		}
	}
	public void download(){
		for(int i=0;i<listURI.size();i++)
		{
			//create folder by splitting the uri
			String path = listURI.get(i);
			String[] spath = path.split("/");
			String fpath = rootFolder;
			String filename = spath[spath.length-1];
			
			//creates paths for the file (filename = last spath).
			for(int j=4;j<spath.length-1;j++)
			{
				fpath = fpath+FileSystems.getDefault().getSeparator()+spath[j];
			}
			Path filePath = Paths.get(fpath);
			try {
				Files.createDirectories(filePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dispose();
			}
			
			//show what file will be download:
			lblCurfile.setText(filename);
			
			//download the file
			try {
				URL fileuri = new URL(listURI.get(i));
				ReadableByteChannel rbc = Channels.newChannel(fileuri.openStream());
				fos = new FileOutputStream(fpath+FileSystems.getDefault().getSeparator()+filename);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.flush();
				fos.close();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			downloaded++;
			progressBar.setValue(downloaded);
			lblDltobedl.setText(downloaded+"/"+numURI);
		}
		//download is finish
		btnCancel.setEnabled(false);
		btnClose.setEnabled(true);
		lblStatusm.setText("Finish!");
	}
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	public JLabel getLblTorrentname() {
		return lblTorrentname;
	}
	public JLabel getLblDltobedl() {
		return lblDltobedl;
	}
	public JLabel getLblCurfile() {
		return lblCurfile;
	}
	public JLabel getLblStatusm() {
		return lblStatusm;
	}
	public JButton getBtnClose() {
		return btnClose;
	}
	public JButton getBtnCancel() {
		return btnCancel;
	}
}
