import java.awt.*;
import javax.swing.*;

///////////////////////////////////////////////////////////////////////////////////////////
//
//	????? "Canvas" ??????????? ?? ?????? ?????? "JComponent" ????? ???????? ????????? ? ??? ??????????? ??????? 
//
public class Canvas extends JComponent 
{
	Id_Face parent;		// ???? - ?????? ?? ?????? "Id_Face" ?? ???????? ??? ?????? ??????? ????????? ??????? "Canvas".
	int Max_X;			// ???? - ?????? ???????????? ?????? ?? ??? ?
	int Max_Y;			// ???? - ?????? ???????????? ?????? ?? ??? Y

	////////////////////////////////////////////////////////////////////////////////////////
	// 	?????, ???????????????? ??????? ?????? ???? ??? ??????????
	//
	public void paintComponent(Graphics g)
	{
		int 	X,Y;					// ???????? ?????
		Color 	Color1;					// ???? ???????? ???????
		
		super.paintComponents(g);		
		Graphics2D g2d=(Graphics2D) g;

		for (Y=0; Y<this.Max_Y; Y++)
		{
			for (X=0; X<this.Max_X; X++)
			{
				Color1 = new Color((int) this.parent.BMP_File_Input.Red[X][Y], (int) this.parent.BMP_File_Input.Green[X][Y], (int) this.parent.BMP_File_Input.Blue[X][Y]);   
				g2d.setColor(Color1);
				g2d.drawLine(X,Y,X,Y);
			}
		}
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	// 	??????????? ?????? "Canvas"
	//
	//	??????? ????????? : parent - ????????? ?? ?????? ?????? "Id_Face" ?? ???????? ??????? ???????? 
	//								 ???????? ?????????? ??????? ?????? "Canvas".
	//
	Canvas(Id_Face parent)
	{
		this.parent = parent;						// ????????? ????????? ?? ?????? ?????? "Id_Face" ?? ???????? ??????? ???????? ???????? ?????????? ??????? ?????? "Canvas"
	  	this.Max_X 	= parent.BMP_File_Input.Max_X;	// ?????????? ?????? ???????????? ?????? ?? ??? ? ?????? ??????? ??????????? ?? ?????????? BMP-?????
	  	this.Max_Y 	= parent.BMP_File_Input.Max_Y;	// ?????????? ?????? ???????????? ?????? ?? ??? Y ?????? ??????? ??????????? ?? ?????????? BMP-?????
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	

	
	
}

///////////////////////////////////////////////////////////////////////////////////////////


