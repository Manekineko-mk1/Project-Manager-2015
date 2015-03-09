package JVMBEK;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class CreateScreen extends Screen {
	public final static String IDENTIFIER = "CREATE";
	public final static int WIDTH = 300;
	public final static int HEIGHT = 300;
	
	private JTextField _nameField;
	private JTextArea _descField;
	//private JTextField _durField;

	public CreateScreen(ScreenManager manager) {
		super(manager);
	}

	@Override
	public void SetupGUI() {
		JPanel northPanel = new JPanel();
		JPanel centerPanel = new JPanel();
		JPanel southPanel = new JPanel();
		
		JLabel pjName = new JLabel("Project Name:");
		JLabel pjDis  = new JLabel("Project Description:");
		//JLabel pjDur = new JLabel("Project Duration: ");
		
		_nameField = new JTextField(10);
		_descField = new JTextArea();
		JScrollPane scroll = new JScrollPane(_descField);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//_durField = new JTextField(10);
		
		JButton btnCreate = new JButton("Create Project");
		JButton btnCancel = new JButton("Cancel");
		northPanel.add(pjName);
		northPanel.add(_nameField);
		//northPanel.add(pjDur);
		//northPanel.add(_durField);
		northPanel.setLayout(new GridLayout(2,2,5,5));
		
		centerPanel.add(pjDis);
		centerPanel.add(scroll);
		centerPanel.setLayout(new GridLayout(2,1));
		
		southPanel.add(btnCreate);
		southPanel.add(btnCancel);
		southPanel.setLayout(new FlowLayout());;
		
		setLayout(new BorderLayout());
		add(BorderLayout.NORTH,northPanel);
		add(BorderLayout.CENTER,centerPanel);
		add(BorderLayout.SOUTH,southPanel);
		
		 btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae){
				if(_nameField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null,
							  "Please enter a name for your project.",
							  "Missing field",
							  JOptionPane.ERROR_MESSAGE);
						return;
				}
				
				if(!Character.isLetter(_nameField.getText().charAt(0))) {
					JOptionPane.showMessageDialog(null,
							  "Project name must begin with a letter.",
							  "Incorrect naming",
							  JOptionPane.ERROR_MESSAGE);
						return;
				}
				_manager.getProjectManager().addNewProject(_nameField.getText(), _descField.getText());
				_manager.showAndResize(ManagerMainScreen.IDENTIFIER, 300, 180);
			}
		 });
		 
		 btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae){
				_manager.showAndResize(ManagerMainScreen.IDENTIFIER, 300, 180);
			}
		 });
	}

	
	@Override
	public void Update() {
		_nameField.setText("");
		_descField.setText("");
		//_durField.setText("");
	}

}
