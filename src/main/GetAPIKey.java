package main;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GetAPIKey extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5356655549134600902L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtApikey;
	private final String userFilePathS = System.getProperty("user.home")+FileSystems.getDefault().getSeparator()+".justseedgui"+FileSystems.getDefault().getSeparator()+"config.conf";
	private final Path userFilePath = Paths.get(userFilePathS); 

	/**
	 * Create the dialog.
	 */
	public GetAPIKey() {
		setResizable(false);
		setModal(true);
		setBounds(100, 100, 450, 242);
		setAlwaysOnTop(true);
		setTitle("API Key setting");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
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
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			JLabel lblMessagegetapikeys = new JLabel("Please, enter your API Key.");
			contentPanel.add(lblMessagegetapikeys, "2, 2, 3, 1");
		}
		{
			JLabel lblYourApiKey = new JLabel("Your API Key:");
			contentPanel.add(lblYourApiKey, "2, 4, right, default");
		}
		{
			txtApikey = new JTextField();
			contentPanel.add(txtApikey, "4, 4, fill, default");
			txtApikey.setColumns(10);
		}
		{
			JLabel lblIfYouDont = new JLabel("<html>\r\n<p>If you don't had an API Key, go to:<br /></p></html>");
			contentPanel.add(lblIfYouDont, "2, 10, 3, 1, default, fill");
		}
		{
			JLabel lblUrlhtml = new JLabel("<html>\r\n<p><u><a>http://justseed.it/options/index.csp</a></u><br /></p></html>\r\n");
			lblUrlhtml.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					//open URI into browser
					Desktop desktop = Desktop.getDesktop();
					try {
						URI uri = new URI("http://justseed.it/options/index.csp");
						desktop.browse(uri);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			lblUrlhtml.setForeground(Color.BLUE);
			contentPanel.add(lblUrlhtml, "2, 12");
		}
		{
			JLabel lblIfyoud = new JLabel("<html><p>and check \"Use API?\" and press \"Apply\", this would generate an API Key.<br /></p>\r\n</html>");
			contentPanel.add(lblIfyoud, "2, 14, 3, 1");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							Files.write(userFilePath, txtApikey.getText().getBytes());
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setVisible(true);
	}

	public JTextField getTxtApikey() {
		return txtApikey;
	}
}
