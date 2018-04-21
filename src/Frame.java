import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import java.awt.Font;

public class Frame extends JFrame {

	private JPanel contentPane;
	private JTextField jtfSource;
	private JTextField jtfCible;
	private JLabel lblSource;
	private JLabel lblCible;
	private JProgressBar progressBar;
	private JScrollPane scrollPane;
	private JTextArea area;

	/**
	 * Create the frame.
	 */
	
	public Frame() {
		setFont(new Font("Arial", Font.PLAIN, 20));
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			JOptionPane.showMessageDialog(null, "Inavlid LookAndFeel", "Look And Feel", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		setTitle("Manga");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 700, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[33sp,grow][33sp,grow][33sp,grow]", "[][][][][grow]"));
		this.setLocationRelativeTo(null);
		
		area = new JTextArea();
		area.setEditable(false);
		
		scrollPane = new JScrollPane(area);
		contentPane.add(scrollPane, "cell 0 4 3 1,grow");
		
		lblSource = new JLabel("Source:");
		contentPane.add(lblSource, "flowx,cell 0 0");
		
		jtfSource = new JTextField();
		contentPane.add(jtfSource, "cell 0 0,growx");
		jtfSource.setColumns(10);
		
		lblCible = new JLabel("Target:");
		contentPane.add(lblCible, "flowx,cell 2 0");
		
		jtfCible = new JTextField();
		contentPane.add(jtfCible, "cell 2 0,growx");
		jtfCible.setColumns(10);
		
		JButton buttonGO = new JButton("GO");
		buttonGO.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				ActionPrinc action = new ActionPrinc("Rename");
				action.start();
				
			}});
		contentPane.add(buttonGO, "cell 1 1,alignx center");
		
		progressBar = new JProgressBar();
		contentPane.add(progressBar, "cell 0 3 3 1,grow");
		
		
		this.setVisible(true);
	}
	
	
	
	
	public long folderSize(File directory) {
		long size = 0;
		
		for(File file : directory.listFiles()) {
			if(file.isFile()) {
				size += file.length();
			}else {
				size += folderSize(file);
			}
		}

		return size;
	}
	
	
	public void refreshGUI() {
		this.revalidate();
	}
	
	
	
	
	class ActionPrinc extends Thread {
		public ActionPrinc(String name) {
			super(name);
		}
	
		
		public void run() {
			
			Path source = Paths.get(jtfSource.getText());
			Path cible = Paths.get(jtfCible.getText());
			
			double processing = 0;
			double toProcess = folderSize(source.toFile());
			progressBar.setMinimum(0);
			progressBar.setMaximum(100000);

			try(DirectoryStream<Path> root = Files.newDirectoryStream(source)){// CONTENU PRINCIPAL // DOSSIER SCANS
				
				for(Path doss : root) { // DOSS = DOSSIERS
					if(Files.isDirectory(doss)) {
						try(DirectoryStream<Path> contDoss = Files.newDirectoryStream(doss, "*.cbz")){ // LES CBZ DIRECTS
							
							Path[] paths = new Path[(int) Files.list(doss).count()];
							int k = 0;
							for(Path cbz : contDoss) {
								paths[k] = cbz;
								k++;
							}
							
							Arrays.sort(paths, new Comparator<Path>() {
								public int compare(Path p1, Path p2) {
									return Long.compare(p1.toFile().lastModified(), p2.toFile().lastModified());
								}
							});
							
							int i = 0;
							int j = (int)Files.list(doss).count();
							String zero = "";
							while(j > 10) {
								j = j/10;
								zero = zero+"0";
							}
							j = 1;
							
							for(Path cbz : paths) {

								if(i == j*10) {
									zero = zero.substring(0, zero.length()-1);
									j = i;
								}

								Path cibleDef = Paths.get(cible+File.separator+doss.getFileName()+File.separator+doss.getFileName()+" "+zero+i+".cbz");
								
								boolean exist = false;
								try{
									Files.createDirectories(cibleDef);
								}catch(FileAlreadyExistsException e) {
									area.append(System.getProperty("line.separator")+"Already Existing");
									exist = true;
								}finally {
									area.append(System.getProperty("line.separator")+cbz.toString());
									area.setCaretPosition(area.getText().length()-cbz.toString().length());;
									
									processing += cbz.toFile().length();
									double temp = processing/toProcess;
									progressBar.setValue((int) (temp*100000));
								}
								
								if(exist == false) {
									Files.copy(cbz, cibleDef, StandardCopyOption.REPLACE_EXISTING);
								}								
								
								
								i++;
															
							}
						}
					}
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Invalid Source or Cible", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			area.append(System.getProperty("line.separator")+"FINISH");
			area.setCaretPosition(area.getText().length());
			JOptionPane.showMessageDialog(null, "The copy of the files is complete.", "FINISH", JOptionPane.INFORMATION_MESSAGE);
			
		}
		}
	}

