import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MakeASale extends JFrame {
    private JTextField searchField;
    private JButton searchBtn;

   public MakeASale() {
       //Window Setup
       setTitle("Make a Sale");
       setSize(1000, 600);
       setLocationRelativeTo(null);
       setDefaultCloseOperation(DISPOSE_ON_CLOSE);
       setVisible(true);


       //pannel = container
       JPanel panel = new JPanel();
       panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
       panel.setLayout(new GridLayout(3,2,10,10));


       //Elements
       JLabel searchLabel = new JLabel("Search Product");
        searchField = new JTextField();
        searchBtn = new JButton("Search");


       //Add elements to panel
       panel.add(searchLabel);
       panel.add(searchField);
       panel.add(searchBtn);

       //Add panel to frame
       add(panel);



       //Action Listeners
       searchBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               Search();
           }
       });
   }



    private void Search(){
       String searchText = searchField.getText();
       System.out.println(searchText);

    }
}






/*        JOptionPane.showMessageDialog(this,
                "Text:\n" + searchText,
                "Search Text",
                JOptionPane.INFORMATION_MESSAGE); */
