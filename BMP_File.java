import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


// Класс "BMP_File" содержит методы работы с BMP-файлами. Их создания, записи в них информации и сохранения на диске.
//

public class BMP_File 
{
	String					File_Name;				// Поле - имя BMP-файла 

	FileInputStream 		File_Input_Stream;		// Поле - Указатель на поток ввода в BMP-файл
	BufferedInputStream		File_Input_Buffer;		// Поле - Указатель на буферизированный поток вывода в BMP-файл
	FileOutputStream 		File_Output_Stream;		// Поле - Указатель на поток вывода в BMP-файл
	BufferedOutputStream	File_Output_Buffer;		// Поле - Указатель на буферизированный поток вывода в BMP-файл
 	
	int						Sdwig_obl_dann;			// Поле - Сдвиг в байтах области данных относительно начала BMP-файла
	int						Dlina_zagolovka;		// Поле - Длина в байтах заголовка входного BMP-файла			
	
	int						Max_X;					// Поле - размер по горизонтали области изображения BMP-файла.
	int						Max_Y;					// Поле - размер по вертикали области изображения BMP-файла.
	int						Kol_zwet_plosk;			// Поле - Количество цветовых плоскостей в BMP-файле
	int						Kol_bit_pixel;			// Поле - Количество бит на пиксель в BMP-файле
	int						Type_press_data;		// Поле - Тип сжатия данных в BMP-файле
	int						Size;					// Поле - Размер графического изображения в байтах в BMP-файле
	int						Razr_horizont;			// Поле - Разрешение по горизонтали в BMP-файле
	int						Razr_vertikal;			// Поле - Разрешение по вертикали в BMP-файле
	int						Kol_dobaw_byte;			// Поле - Количество добавочных байт в строке BMP-файла (получается как остаток от целочислительного деления Max_X на 4)	
	int						Size_File;				// Поле - Размер BMP-файла в байтах.
	
	int 					zagolovok[];			// Поле - Указатель на массив предназначенный для приёма заголовка BMP-файла
	int						Red[][];				// Поле - Указатель на матрицу хранящую уровни красной составляющей цвета пикселей изображений
	int						Green[][];				// Поле - Указатель на матрицу хранящую уровни зелёной составляющей цвета пикселей изображений
	int						Blue[][];				// Поле - Указатель на матрицу хранящую уровни голубой составляющей цвета пикселей изображений
	double					RGB[][];				// Поле - Указатель на матрицу хранящую яркости пикселей изображения
	double					Sum_RGB[][];			// Поле - Указатель на матрицу хранящую суммарные яркости всех пикселей выше и левее текущей
	
	int 					Byte4[];				// Поле - Указатель на массив используемый для возврата результата представления целого числа от 0 до 4 294 967 296 в виде 4 х байт 
	int						Code_str[][][];			// Поле - Указатель на кодовыю страницу, которая хранит изображения символов
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Метод устанавливающий значение пикселя с координатами ( X1, Y1 ) матрицы Sum_RGB[][]   
	//
	//		Входные параметры : 		X1 	- Координата X элемента матрицы 
	//									Y1	- Координата Y элемента матрицы
	//								 value 	- Значение которое надо записать в пиксель  
	//
	//	Возвращаемое значение :		Нет
	//				
	void set_Sum_RGB( int X1, int Y1, double value )
	{
		if ((0 <= X1) && (X1 < this.Max_X) && (0 <= Y1) && (Y1 < this.Max_Y))
		{
			// Пиксель с координатами ( X1, Y1 ) лежит внутри матрицы. 
			this.Sum_RGB[X1][Y1] = value;									// Записать в матрицу значение пикселя.
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Метод выдающий значение пикселя с координатами ( X1, Y1 ) матрицы Sum_RGB[][]   
	//
	//		Входные параметры : 	X1 	- Координата X элемента матрицы 
	//								Y1	- Координата Y элемента матрицы
	//
	//	Возвращаемое значение :		Если  ((0 <= X1 < BMP_Input_Max_X) И (0 <= Y1 < BMP_Input_Max_Y))  Тогда	Sum_RGB[X1][Y1] Иначе 0.	  
	//				
	double get_Sum_RGB( int X1, int Y1 )
	{
		double value;	// Значение пикселя считанное из матрицы.
		
		if ((0 <= X1) && (X1 < this.Max_X) && (0 <= Y1) && (Y1 < this.Max_Y))
		{
			// Пиксель с координатами ( X1, Y1 ) лежит внутри матрицы. 
			value = this.Sum_RGB[X1][Y1];									// Прочитать из матрицы значение пикселя.
		}
		else
		{
			// Пиксель с координатами ( X1, Y1 ) лежит за пределами матрицы. 
			value = 0.0;												//	Выдать 0.
		}
		
		return value;		//  Выход из метода с возвратом считанного значения пикселя 
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Метод устанавливающий значение пикселя с координатами (X1, Y1). 
	//
	//	Выполняет запись в матрицы : Blue[][] - голубого цвета, Green[][] - зелёного цвета,	
	//								 Red[][]  - красного цвета,	RGB[][]	  - суммарна яркость				
	//
	//		Входные параметры : 		X1,Y1  - Координаты X,Y пикселя 
	//									Blue1  - Уровень голубого																				
	//									Green1 - Уровень зелёного 
	//								    Red1   - Уровень красного	
	//
	//	Возвращаемое значение :		Нет
	//				

	void set_Рixel( int X1, int Y1, int Blue1, int Green1, int Red1 )
	{
		if ((0 <= X1) && (X1 < this.Max_X) && (0 <= Y1) && (Y1 < this.Max_Y))
		{
			// Пиксель с координатами ( X1, Y1 ) лежит внутри матрицы. 
			this.Blue[X1][Y1] 	= Blue1;			// Записать в матрицу голубого цвета значение голубого пикселя.
			this.Green[X1][Y1] 	= Green1;			// Записать в матрицу зелёного цвета значение зелёного пикселя.			
			this.Red[X1][Y1] 	= Red1;				// Записать в матрицу красного цвета значение красного пикселя.				
			this.RGB[X1][Y1] 	= (double) Blue1 + (double) Green1 + (double) Red1;	 // Записать в матрицу суммарной яркости значение сумарной яркости трёх цветовых составляющих пикселя	    
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Метод выполняющий вывод текстовой надписи в BMP-файл 
	//
	//		Входные параметры : 		X_lew, Y_werh  	- Координаты левого верхнего угла надписи	
	//											Text	- Текстовая строка которую надо выдать 
	
	
	void Write_text( int X_lew, int Y_werh, String Text  )
	{
		int Dlina_Text;		// Длина текстовой строки
		int poz, X, Y;		// Счётчики цикла
		int Code_symbol;	// Код символа	
		int Blue1;			// Уровень голубой составляющей 
		int Green1; 		// Уровень зелёной составляющей
		int Red1;			// Уровень красной составляющей
		
		Dlina_Text = Text.length();			// Определить длину строки содержащей искомый текст
//		System.out.println("  Сообщение из Write_text : Длина строки "+Text+"    = "+Dlina_Text);
		
		for (poz=0; poz < Dlina_Text; poz++)
		{
			Code_symbol = (int) Text.charAt(poz);	// Получить код очередного симаола
//			System.out.println(" Код символа № "+poz+"  = "+Code_symbol);
			
			if ((0 <= Code_symbol) && (Code_symbol <= 255))
			{
				// Код символа лежит в диаазое от 0 до 255.
				for (Y=0; Y<10; Y++)
					for (X=0; X<10; X++)
					{
						if (Code_str[Code_symbol][X][Y] > 0)
						{
							Blue1 	= 0; 
							Green1 	= 0;
							Red1 	= 0;	
						}
						else
						{
							Blue1 	= 255; 
							Green1 	= 255;
							Red1 	= 255;	
						}
						this.set_Рixel(X_lew+X+poz*10, this.Max_Y-(Y_werh+Y), Blue1, Green1, Red1);			 
					}
			}
		}
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Метод выдающий суммарную яркость прямоугольного участка изображения задаваемого координатами 
	//	левого верхнего угла (X1,Y1) и правого нижнего угла (X2,Y2) с помощью матрицы Sum_RGB[][]. 
	//	
	double Sum_Yark_1( int X1, int Y1, int X2, int Y2 )
	{
		double 	Sum_Yark;		// Суммарная яркость прямоугольного участка изображения

		Sum_Yark = get_Sum_RGB(X2,Y2) - get_Sum_RGB(X1-1,Y2) - get_Sum_RGB(X2,Y1-1) + get_Sum_RGB(X1-1,Y1-1);	// Найти суммарную яркость прямоугольного участка изображения  
		
		return Sum_Yark;		// Выход из метода с возвратом найденной суммарной яркости участка изображения
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Метод выдающий суммарную яркость прямоугольного участка изображения задаваемого координатами 
	//	левого верхнего угла (X1,Y1) и правого нижнего угла (X2,Y2) с помощью матрицы RGB[][]. 
	//	
	double Sum_Yark_2( int X1, int Y1, int X2, int Y2 )
	{
		double 	Sum_Yark;		// Суммарная яркость прямоугольного участка изображения
		int X,Y;				// Счётчики цикла
	
		// Найти суммарную яркость прямоугольного участка изображения 
		Sum_Yark = 0;
		for(X=X1; X<=X2; X++) 	for(Y=Y1; Y<=Y2; Y++) Sum_Yark += RGB[X][Y];
		
		return Sum_Yark;		// Выход из метода с возвратом найденной суммарной яркости участка изображения
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Метод выполняющий представление целого числа от 0 до 4 294 967 296 в виде 4 х байт
	//
	//		Входные параметры : 		X 	- Целое число  
	//

	void Convert_int_4byte(int X)
	{
		int b0, b1, b2, b3;
		
//		System.out.println(" Сообщение из функции  Convert_int_4byte   X = "+X);		
		
		b3 = (int) Math.floor(X/16777216);
		X = X - b3 * 16777216;

		b2 = (int) Math.floor(X/65536);
		X = X - b2 * 65536;		

		b1 = (int) Math.floor(X/256);
		X = X - b1 * 256;
		
		b0 = X;
		
		this.Byte4[0] = b0;
		this.Byte4[1] = b1;		
		this.Byte4[2] = b2;		
		this.Byte4[3] = b3;

//		System.out.println(" Сообщение из функции  b0 = "+b0+"   b1= "+b1+"   b2= "+b2+"   b3= "+b3 );		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Конструктор класса "BMP_File".
	//
	//	Входные параметры : BMP_File_Name - Имя BMP-файла. 
	//								Max_X - Размер изображения по горизонтали в пикселях 
	//								Max_Y - Размер изображения по вертикали   в пикселях
	//	

	BMP_File(String BMP_File_Name, int Max_X, int Max_Y)
	{
		int i, X, Y; 								// Счётчик цикла
		
		this.File_Name 			= BMP_File_Name;	// Запомнить имя BMP-файла
		this.Sdwig_obl_dann 	= 54;				// Задать сдвиг в байтах области данных относительно начала BMP-файла
		this.Dlina_zagolovka 	= 40;				// Задать длину в байтах заголовка входного BMP-файла			
		
		this.Max_X 				= Max_X;			// Запомнить размер изображения по горизонтали в пикселях
		this.Max_Y				= Max_Y;			// Запомнить размер изображения по вертикали в пикселях
		
		this.Kol_zwet_plosk 	= 1;				// Задать количество цветовых плоскостей в BMP-файле
		this.Kol_bit_pixel  	= 24;				// Задать количество бит на пиксель в BMP-файле
		this.Type_press_data 	= 0;				// Задать тип сжатия данных в BMP-файле

		this.Kol_dobaw_byte = this.Max_X % 4;							// Определить и записать количество добавочных байт в строке BMP-файла (получается как остаток от целочислительного деления BMP_Input_Max_X на 4).	
		this.Size = (this.Max_X*3 + this.Kol_dobaw_byte) * this.Max_Y;	// Задать размер графического изображения в байтах в BMP-файле
		this.Razr_horizont		= 0;									// Задать разрешение по горизонтали в BMP-файле
		this.Razr_vertikal		= 0;									// Задать разрешение по вертикали в BMP-файле
		
		this.Size_File 	= 54 + (this.Max_X*3 + this.Kol_dobaw_byte) * this.Max_Y;	// Определить и записать размер BMP-файла протокола работы программы в байтах.  						

/*
		System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");		
		System.out.println("                          Вызван конструктор BMP-файла");
		System.out.println("												      ");
		System.out.println("                            Имя файла = "+this.File_Name);
		System.out.println("                 Сдвиг области данных = "+this.Sdwig_obl_dann);		
		System.out.println("                      Длина заголовка = "+this.Dlina_zagolovka);		
		System.out.println("                Размер по горизонтали = "+this.Max_X);
		System.out.println("                  Размер по вертикали = "+this.Max_Y);
		System.out.println("       Количество цветовых плоскостей = "+this.Kol_zwet_plosk);		
		System.out.println("            Количество бит на пиксель = "+this.Kol_bit_pixel);		
		System.out.println("                    Тип сжатия данных = "+this.Type_press_data);		
		System.out.println("     Размер граф.изображения в байтах = "+this.Size);		
		System.out.println("            Разрешение по горизонтали = "+this.Razr_horizont);		
		System.out.println("              Разрешение по вертикали = "+this.Razr_vertikal);		
		System.out.println("  Количество добавочных байт в строке = "+this.Kol_dobaw_byte);
		System.out.println("  Размер BMP-файла протокола в байтах = "+this.Size_File);
*/		
		this.zagolovok 	= new int[54];							// Выделить память для хранения матрицы 54 чисел (заголовок BMP-файла).		
		this.Red		= new int[this.Max_X][this.Max_Y];		// Выделить память для хранения уровней красной составляющей цвета пикселей изображения
		this.Green		= new int[this.Max_X][this.Max_Y];		// Выделить память для хранения уровней зелёной составляющей цвета пикселей изображения
		this.Blue		= new int[this.Max_X][this.Max_Y];		// Выделить память для хранения уровней голубой составляющей цвета пикселей изображения
		this.RGB		= new double[this.Max_X][this.Max_Y];	// Выделить память для хранения матрицы хранящую яркости пикселей изображения
		this.Sum_RGB	= new double[this.Max_X][this.Max_Y];	// Выделить память для хранения матрицы хранящую суммарные яркости всех пикселей выше и левее текущей
		this.Byte4		= new int[4];							// Выделить память для хранения массива использумого для преобразования целого числа от 0 до 4 294 967 296 в виде 4 байта
		this.Code_str	= new int[256][10][10];					// Выделить память для хранения кодовой страницы содержащей образы символов	
		
		// Заполнить заголовок 0.
		for (i=0; i<54; i++) this.zagolovok[i]=0;

		// Записать в заголовок "BM"
		this.zagolovok[0] = 'B';
		this.zagolovok[1] = 'M';		
		
		// Записать в заголовок сдвиг области данных относительно начала файла  в байтах (позиции 10,11,12,13).
		Convert_int_4byte(this.Sdwig_obl_dann);
		this.zagolovok[10] = this.Byte4[0];
		this.zagolovok[11] = this.Byte4[1];		
		this.zagolovok[12] = this.Byte4[2];			
		this.zagolovok[13] = this.Byte4[3];
					
		// Записать в заголовок длину информационного заголовка в байтах (позиции 14,15,16,17).
		Convert_int_4byte(this.Dlina_zagolovka);
		this.zagolovok[14] = this.Byte4[0];
		this.zagolovok[15] = this.Byte4[1];		
		this.zagolovok[16] = this.Byte4[2];			
		this.zagolovok[17] = this.Byte4[3];
		
		// Записать в заголовок длину изображения по горизонтали в пикселях (позиции 18,19,20,21).
		Convert_int_4byte(this.Max_X);		
		this.zagolovok[18] = this.Byte4[0];
		this.zagolovok[19] = this.Byte4[1];		
		this.zagolovok[20] = this.Byte4[2];			
		this.zagolovok[21] = this.Byte4[3];
					
		// Записать в заголовок ширину изображения по вертикали в пикселях (позиции 22,23,24,25).
		Convert_int_4byte(this.Max_Y);		
		this.zagolovok[22] = this.Byte4[0];
		this.zagolovok[23] = this.Byte4[1];		
		this.zagolovok[24] = this.Byte4[2];			
		this.zagolovok[25] = this.Byte4[3];
					
		// Записать в заголовок количество цветовых плоскостей (позиции 26,27).
		Convert_int_4byte(this.Kol_zwet_plosk);		
		this.zagolovok[26] = this.Byte4[0];
		this.zagolovok[27] = this.Byte4[1];		
		
		// Записать в заголовок количество бит на пиксель (позиции 28,29).
		Convert_int_4byte(this.Kol_bit_pixel);
		this.zagolovok[28] = this.Byte4[0];
		this.zagolovok[29] = this.Byte4[1];		
		
		// Записать в заголовок тип сжатия данных (позиции 30,31,32,33)					
		Convert_int_4byte(this.Type_press_data);		
		this.zagolovok[30] = this.Byte4[0];
		this.zagolovok[31] = this.Byte4[1];		
		this.zagolovok[32] = this.Byte4[2];			
		this.zagolovok[33] = this.Byte4[3];
					
		// Записать в заголовок размер графического изображения в байтах (позиции 34,35,36,37)
		Convert_int_4byte(this.Size);		
		this.zagolovok[34] = this.Byte4[0];
		this.zagolovok[35] = this.Byte4[1];		
		this.zagolovok[36] = this.Byte4[2];			
		this.zagolovok[37] = this.Byte4[3];
		
		// Записать в заголовок разрешение по горизонтали (позиции 38,39,40,41)	
		Convert_int_4byte(this.Razr_horizont);
		this.zagolovok[38] = this.Byte4[0];
		this.zagolovok[39] = this.Byte4[1];		
		this.zagolovok[40] = this.Byte4[2];			
		this.zagolovok[41] = this.Byte4[3];
					
		// Записать в заголовок разрешение по вертикали (позиции 42,43,44,45)
		Convert_int_4byte(this.Razr_vertikal);					
		this.zagolovok[42] = this.Byte4[0];
		this.zagolovok[43] = this.Byte4[1];		
		this.zagolovok[44] = this.Byte4[2];			
		this.zagolovok[45] = this.Byte4[3];
		
		// Заполнение 0 массивов : красной RED[][], зелёной GREEN[][], голубой BLUE[][]  составляющих цвета пикселя,
		//                         яркости RGB[][], суммарной яркости всех пикселей левее и выше текущего Sum_RGB[][].  
		for (Y=0; Y<this.Max_Y; Y++)  
			for (X=0; X<this.Max_X; X++)
				{
					this.Green[X][Y] 	= 255;
					this.Blue[X][Y] 	= 255;
					this.Red[X][Y] 		= 255;
					this.RGB[X][Y] 		= 0;
					this.Sum_RGB[X][Y] 	= 0;
				}

		
		// Заполнение кодовой страницы образами символов.
		
		// Символ " "		
		this.Code_str[32][0][0] = 0;	this.Code_str[32][1][0] = 0;	this.Code_str[32][2][0] = 0;	this.Code_str[32][3][0] = 0;	this.Code_str[32][4][0] = 0;	this.Code_str[32][5][0] = 0;	this.Code_str[32][6][0] = 0;	this.Code_str[32][7][0] = 0;	this.Code_str[32][8][0] = 0;	this.Code_str[32][9][0] = 0;		
		this.Code_str[32][0][1] = 0;	this.Code_str[32][1][1] = 0;	this.Code_str[32][2][1] = 0;	this.Code_str[32][3][1] = 0;	this.Code_str[32][4][1] = 0;	this.Code_str[32][5][1] = 0;	this.Code_str[32][6][1] = 0;	this.Code_str[32][7][1] = 0;	this.Code_str[32][8][1] = 0;	this.Code_str[32][9][1] = 0;		
		this.Code_str[32][0][2] = 0;	this.Code_str[32][1][2] = 0;	this.Code_str[32][2][2] = 0;	this.Code_str[32][3][2] = 0;	this.Code_str[32][4][2] = 0;	this.Code_str[32][5][2] = 0;	this.Code_str[32][6][2] = 0;	this.Code_str[32][7][2] = 0;	this.Code_str[32][8][2] = 0;	this.Code_str[32][9][2] = 0;		
		this.Code_str[32][0][3] = 0;	this.Code_str[32][1][3] = 0;	this.Code_str[32][2][3] = 0;	this.Code_str[32][3][3] = 0;	this.Code_str[32][4][3] = 0;	this.Code_str[32][5][3] = 0;	this.Code_str[32][6][3] = 0;	this.Code_str[32][7][3] = 0;	this.Code_str[32][8][3] = 0;	this.Code_str[32][9][3] = 0;		
		this.Code_str[32][0][4] = 0;	this.Code_str[32][1][4] = 0;	this.Code_str[32][2][4] = 0;	this.Code_str[32][3][4] = 0;	this.Code_str[32][4][4] = 0;	this.Code_str[32][5][4] = 0;	this.Code_str[32][6][4] = 0;	this.Code_str[32][7][4] = 0;	this.Code_str[32][8][4] = 0;	this.Code_str[32][9][4] = 0;		
		this.Code_str[32][0][5] = 0;	this.Code_str[32][1][5] = 0;	this.Code_str[32][2][5] = 0;	this.Code_str[32][3][5] = 0;	this.Code_str[32][4][5] = 0;	this.Code_str[32][5][5] = 0;	this.Code_str[32][6][5] = 0;	this.Code_str[32][7][5] = 0;	this.Code_str[32][8][5] = 0;	this.Code_str[32][9][5] = 0;		
		this.Code_str[32][0][6] = 0;	this.Code_str[32][1][6] = 0;	this.Code_str[32][2][6] = 0;	this.Code_str[32][3][6] = 0;	this.Code_str[32][4][6] = 0;	this.Code_str[32][5][6] = 0;	this.Code_str[32][6][6] = 0;	this.Code_str[32][7][6] = 0;	this.Code_str[32][8][6] = 0;	this.Code_str[32][9][6] = 0;		
		this.Code_str[32][0][7] = 0;	this.Code_str[32][1][7] = 0;	this.Code_str[32][2][7] = 0;	this.Code_str[32][3][7] = 0;	this.Code_str[32][4][7] = 0;	this.Code_str[32][5][7] = 0;	this.Code_str[32][6][7] = 0;	this.Code_str[32][7][7] = 0;	this.Code_str[32][8][7] = 0;	this.Code_str[32][9][7] = 0;		
		this.Code_str[32][0][8] = 0;	this.Code_str[32][1][8] = 0;	this.Code_str[32][2][8] = 0;	this.Code_str[32][3][8] = 0;	this.Code_str[32][4][8] = 0;	this.Code_str[32][5][8] = 0;	this.Code_str[32][6][8] = 0;	this.Code_str[32][7][8] = 0;	this.Code_str[32][8][8] = 0;	this.Code_str[32][9][8] = 0;		
		this.Code_str[32][0][9] = 0;	this.Code_str[32][1][9] = 0;	this.Code_str[32][2][9] = 0;	this.Code_str[32][3][9] = 0;	this.Code_str[32][4][9] = 0;	this.Code_str[32][5][9] = 0;	this.Code_str[32][6][9] = 0;	this.Code_str[32][7][9] = 0;	this.Code_str[32][8][9] = 0;	this.Code_str[32][9][9] = 0;		
		
		// Символ "-"		
		this.Code_str[45][0][0] = 0;	this.Code_str[45][1][0] = 0;	this.Code_str[45][2][0] = 0;	this.Code_str[45][3][0] = 0;	this.Code_str[45][4][0] = 0;	this.Code_str[45][5][0] = 0;	this.Code_str[45][6][0] = 0;	this.Code_str[45][7][0] = 0;	this.Code_str[45][8][0] = 0;	this.Code_str[45][9][0] = 0;		
		this.Code_str[45][0][1] = 0;	this.Code_str[45][1][1] = 0;	this.Code_str[45][2][1] = 0;	this.Code_str[45][3][1] = 0;	this.Code_str[45][4][1] = 0;	this.Code_str[45][5][1] = 0;	this.Code_str[45][6][1] = 0;	this.Code_str[45][7][1] = 0;	this.Code_str[45][8][1] = 0;	this.Code_str[45][9][1] = 0;		
		this.Code_str[45][0][2] = 0;	this.Code_str[45][1][2] = 0;	this.Code_str[45][2][2] = 0;	this.Code_str[45][3][2] = 0;	this.Code_str[45][4][2] = 0;	this.Code_str[45][5][2] = 0;	this.Code_str[45][6][2] = 0;	this.Code_str[45][7][2] = 0;	this.Code_str[45][8][2] = 0;	this.Code_str[45][9][2] = 0;		
		this.Code_str[45][0][3] = 0;	this.Code_str[45][1][3] = 0;	this.Code_str[45][2][3] = 0;	this.Code_str[45][3][3] = 0;	this.Code_str[45][4][3] = 0;	this.Code_str[45][5][3] = 0;	this.Code_str[45][6][3] = 0;	this.Code_str[45][7][3] = 0;	this.Code_str[45][8][3] = 0;	this.Code_str[45][9][3] = 0;		
		this.Code_str[45][0][4] = 0;	this.Code_str[45][1][4] = 0;	this.Code_str[45][2][4] = 1;	this.Code_str[45][3][4] = 1;	this.Code_str[45][4][4] = 1;	this.Code_str[45][5][4] = 1;	this.Code_str[45][6][4] = 1;	this.Code_str[45][7][4] = 1;	this.Code_str[45][8][4] = 0;	this.Code_str[45][9][4] = 0;		
		this.Code_str[45][0][5] = 0;	this.Code_str[45][1][5] = 0;	this.Code_str[45][2][5] = 0;	this.Code_str[45][3][5] = 0;	this.Code_str[45][4][5] = 0;	this.Code_str[45][5][5] = 0;	this.Code_str[45][6][5] = 0;	this.Code_str[45][7][5] = 0;	this.Code_str[45][8][5] = 0;	this.Code_str[45][9][5] = 0;		
		this.Code_str[45][0][6] = 0;	this.Code_str[45][1][6] = 0;	this.Code_str[45][2][6] = 0;	this.Code_str[45][3][6] = 0;	this.Code_str[45][4][6] = 0;	this.Code_str[45][5][6] = 0;	this.Code_str[45][6][6] = 0;	this.Code_str[45][7][6] = 0;	this.Code_str[45][8][6] = 0;	this.Code_str[45][9][6] = 0;		
		this.Code_str[45][0][7] = 0;	this.Code_str[45][1][7] = 0;	this.Code_str[45][2][7] = 0;	this.Code_str[45][3][7] = 0;	this.Code_str[45][4][7] = 0;	this.Code_str[45][5][7] = 0;	this.Code_str[45][6][7] = 0;	this.Code_str[45][7][7] = 0;	this.Code_str[45][8][7] = 0;	this.Code_str[45][9][7] = 0;		
		this.Code_str[45][0][8] = 0;	this.Code_str[45][1][8] = 0;	this.Code_str[45][2][8] = 0;	this.Code_str[45][3][8] = 0;	this.Code_str[45][4][8] = 0;	this.Code_str[45][5][8] = 0;	this.Code_str[45][6][8] = 0;	this.Code_str[45][7][8] = 0;	this.Code_str[45][8][8] = 0;	this.Code_str[45][9][8] = 0;		
		this.Code_str[45][0][9] = 0;	this.Code_str[45][1][9] = 0;	this.Code_str[45][2][9] = 0;	this.Code_str[45][3][9] = 0;	this.Code_str[45][4][9] = 0;	this.Code_str[45][5][9] = 0;	this.Code_str[45][6][9] = 0;	this.Code_str[45][7][9] = 0;	this.Code_str[45][8][9] = 0;	this.Code_str[45][9][9] = 0;		
		
		// Символ "."		
		this.Code_str[46][0][0] = 0;	this.Code_str[46][1][0] = 0;	this.Code_str[46][2][0] = 0;	this.Code_str[46][3][0] = 0;	this.Code_str[46][4][0] = 0;	this.Code_str[46][5][0] = 0;	this.Code_str[46][6][0] = 0;	this.Code_str[46][7][0] = 0;	this.Code_str[46][8][0] = 0;	this.Code_str[46][9][0] = 0;		
		this.Code_str[46][0][1] = 0;	this.Code_str[46][1][1] = 0;	this.Code_str[46][2][1] = 0;	this.Code_str[46][3][1] = 0;	this.Code_str[46][4][1] = 0;	this.Code_str[46][5][1] = 0;	this.Code_str[46][6][1] = 0;	this.Code_str[46][7][1] = 0;	this.Code_str[46][8][1] = 0;	this.Code_str[46][9][1] = 0;		
		this.Code_str[46][0][2] = 0;	this.Code_str[46][1][2] = 0;	this.Code_str[46][2][2] = 0;	this.Code_str[46][3][2] = 0;	this.Code_str[46][4][2] = 0;	this.Code_str[46][5][2] = 0;	this.Code_str[46][6][2] = 0;	this.Code_str[46][7][2] = 0;	this.Code_str[46][8][2] = 0;	this.Code_str[46][9][2] = 0;		
		this.Code_str[46][0][3] = 0;	this.Code_str[46][1][3] = 0;	this.Code_str[46][2][3] = 0;	this.Code_str[46][3][3] = 0;	this.Code_str[46][4][3] = 0;	this.Code_str[46][5][3] = 0;	this.Code_str[46][6][3] = 0;	this.Code_str[46][7][3] = 0;	this.Code_str[46][8][3] = 0;	this.Code_str[46][9][3] = 0;		
		this.Code_str[46][0][4] = 0;	this.Code_str[46][1][4] = 0;	this.Code_str[46][2][4] = 0;	this.Code_str[46][3][4] = 0;	this.Code_str[46][4][4] = 0;	this.Code_str[46][5][4] = 0;	this.Code_str[46][6][4] = 0;	this.Code_str[46][7][4] = 0;	this.Code_str[46][8][4] = 0;	this.Code_str[46][9][4] = 0;		
		this.Code_str[46][0][5] = 0;	this.Code_str[46][1][5] = 0;	this.Code_str[46][2][5] = 0;	this.Code_str[46][3][5] = 0;	this.Code_str[46][4][5] = 0;	this.Code_str[46][5][5] = 0;	this.Code_str[46][6][5] = 0;	this.Code_str[46][7][5] = 0;	this.Code_str[46][8][5] = 0;	this.Code_str[46][9][5] = 0;		
		this.Code_str[46][0][6] = 0;	this.Code_str[46][1][6] = 0;	this.Code_str[46][2][6] = 0;	this.Code_str[46][3][6] = 0;	this.Code_str[46][4][6] = 0;	this.Code_str[46][5][6] = 0;	this.Code_str[46][6][6] = 0;	this.Code_str[46][7][6] = 0;	this.Code_str[46][8][6] = 0;	this.Code_str[46][9][6] = 0;		
		this.Code_str[46][0][7] = 0;	this.Code_str[46][1][7] = 0;	this.Code_str[46][2][7] = 0;	this.Code_str[46][3][7] = 0;	this.Code_str[46][4][7] = 0;	this.Code_str[46][5][7] = 0;	this.Code_str[46][6][7] = 0;	this.Code_str[46][7][7] = 0;	this.Code_str[46][8][7] = 0;	this.Code_str[46][9][7] = 0;		
		this.Code_str[46][0][8] = 0;	this.Code_str[46][1][8] = 0;	this.Code_str[46][2][8] = 0;	this.Code_str[46][3][8] = 0;	this.Code_str[46][4][8] = 1;	this.Code_str[46][5][8] = 1;	this.Code_str[46][6][8] = 0;	this.Code_str[46][7][8] = 0;	this.Code_str[46][8][8] = 0;	this.Code_str[46][9][8] = 0;		
		this.Code_str[46][0][9] = 0;	this.Code_str[46][1][9] = 0;	this.Code_str[46][2][9] = 0;	this.Code_str[46][3][9] = 0;	this.Code_str[46][4][9] = 1;	this.Code_str[46][5][9] = 1;	this.Code_str[46][6][9] = 0;	this.Code_str[46][7][9] = 0;	this.Code_str[46][8][9] = 0;	this.Code_str[46][9][9] = 0;		
		
		// Символ "0" 
		this.Code_str[48][0][0] = 0;	this.Code_str[48][1][0] = 0;	this.Code_str[48][2][0] = 0;	this.Code_str[48][3][0] = 0;	this.Code_str[48][4][0] = 1;	this.Code_str[48][5][0] = 1;	this.Code_str[48][6][0] = 0;	this.Code_str[48][7][0] = 0;	this.Code_str[48][8][0] = 0;	this.Code_str[48][9][0] = 0;		
		this.Code_str[48][0][1] = 0;	this.Code_str[48][1][1] = 0;	this.Code_str[48][2][1] = 0;	this.Code_str[48][3][1] = 1;	this.Code_str[48][4][1] = 0;	this.Code_str[48][5][1] = 0;	this.Code_str[48][6][1] = 1;	this.Code_str[48][7][1] = 0;	this.Code_str[48][8][1] = 0;	this.Code_str[48][9][1] = 0;		
		this.Code_str[48][0][2] = 0;	this.Code_str[48][1][2] = 0;	this.Code_str[48][2][2] = 1;	this.Code_str[48][3][2] = 0;	this.Code_str[48][4][2] = 0;	this.Code_str[48][5][2] = 0;	this.Code_str[48][6][2] = 0;	this.Code_str[48][7][2] = 1;	this.Code_str[48][8][2] = 0;	this.Code_str[48][9][2] = 0;		
		this.Code_str[48][0][3] = 0;	this.Code_str[48][1][3] = 0;	this.Code_str[48][2][3] = 1;	this.Code_str[48][3][3] = 0;	this.Code_str[48][4][3] = 0;	this.Code_str[48][5][3] = 0;	this.Code_str[48][6][3] = 0;	this.Code_str[48][7][3] = 1;	this.Code_str[48][8][3] = 0;	this.Code_str[48][9][3] = 0;		
		this.Code_str[48][0][4] = 0;	this.Code_str[48][1][4] = 0;	this.Code_str[48][2][4] = 1;	this.Code_str[48][3][4] = 0;	this.Code_str[48][4][4] = 0;	this.Code_str[48][5][4] = 0;	this.Code_str[48][6][4] = 0;	this.Code_str[48][7][4] = 1;	this.Code_str[48][8][4] = 0;	this.Code_str[48][9][4] = 0;		
		this.Code_str[48][0][5] = 0;	this.Code_str[48][1][5] = 0;	this.Code_str[48][2][5] = 1;	this.Code_str[48][3][5] = 0;	this.Code_str[48][4][5] = 0;	this.Code_str[48][5][5] = 0;	this.Code_str[48][6][5] = 0;	this.Code_str[48][7][5] = 1;	this.Code_str[48][8][5] = 0;	this.Code_str[48][9][5] = 0;		
		this.Code_str[48][0][6] = 0;	this.Code_str[48][1][6] = 0;	this.Code_str[48][2][6] = 1;	this.Code_str[48][3][6] = 0;	this.Code_str[48][4][6] = 0;	this.Code_str[48][5][6] = 0;	this.Code_str[48][6][6] = 0;	this.Code_str[48][7][6] = 1;	this.Code_str[48][8][6] = 0;	this.Code_str[48][9][6] = 0;		
		this.Code_str[48][0][7]	= 0;	this.Code_str[48][1][7] = 0;	this.Code_str[48][2][7] = 1;	this.Code_str[48][3][7] = 0;	this.Code_str[48][4][7] = 0;	this.Code_str[48][5][7] = 0;	this.Code_str[48][6][7] = 0;	this.Code_str[48][7][7] = 1;	this.Code_str[48][8][7] = 0;	this.Code_str[48][9][7] = 0;		
		this.Code_str[48][0][8] = 0;	this.Code_str[48][1][8] = 0;	this.Code_str[48][2][8] = 0;	this.Code_str[48][3][8] = 1;	this.Code_str[48][4][8] = 0;	this.Code_str[48][5][8] = 0;	this.Code_str[48][6][8] = 1;	this.Code_str[48][7][8] = 0;	this.Code_str[48][8][8] = 0;	this.Code_str[48][9][8] = 0;		
		this.Code_str[48][0][9] = 0;	this.Code_str[48][1][9] = 0;	this.Code_str[48][2][9] = 0;	this.Code_str[48][3][9] = 0;	this.Code_str[48][4][9] = 1;	this.Code_str[48][5][9] = 1;	this.Code_str[48][6][9] = 0;	this.Code_str[48][7][9] = 0;	this.Code_str[48][8][9] = 0;	this.Code_str[48][9][9] = 0;		
		
		// Символ "1" 
		this.Code_str[49][0][0] = 0;	this.Code_str[49][1][0] = 0;	this.Code_str[49][2][0] = 0;	this.Code_str[49][3][0] = 0;	this.Code_str[49][4][0] = 0;	this.Code_str[49][5][0] = 1;	this.Code_str[49][6][0] = 0;	this.Code_str[49][7][0] = 0;	this.Code_str[49][8][0] = 0;	this.Code_str[49][9][0] = 0;		
		this.Code_str[49][0][1] = 0;	this.Code_str[49][1][1] = 0;	this.Code_str[49][2][1] = 0;	this.Code_str[49][3][1] = 0;	this.Code_str[49][4][1] = 1;	this.Code_str[49][5][1] = 1;	this.Code_str[49][6][1] = 0;	this.Code_str[49][7][1] = 0;	this.Code_str[49][8][1] = 0;	this.Code_str[49][9][1] = 0;		
		this.Code_str[49][0][2] = 0;	this.Code_str[49][1][2] = 0;	this.Code_str[49][2][2] = 0;	this.Code_str[49][3][2] = 1;	this.Code_str[49][4][2] = 0;	this.Code_str[49][5][2] = 1;	this.Code_str[49][6][2] = 0;	this.Code_str[49][7][2] = 0;	this.Code_str[49][8][2] = 0;	this.Code_str[49][9][2] = 0;		
		this.Code_str[49][0][3] = 0;	this.Code_str[49][1][3] = 0;	this.Code_str[49][2][3] = 1;	this.Code_str[49][3][3] = 0;	this.Code_str[49][4][3] = 0;	this.Code_str[49][5][3] = 1;	this.Code_str[49][6][3] = 0;	this.Code_str[49][7][3] = 0;	this.Code_str[49][8][3] = 0;	this.Code_str[49][9][3] = 0;		
		this.Code_str[49][0][4] = 0;	this.Code_str[49][1][4] = 0;	this.Code_str[49][2][4] = 0;	this.Code_str[49][3][4] = 0;	this.Code_str[49][4][4] = 0;	this.Code_str[49][5][4] = 1;	this.Code_str[49][6][4] = 0;	this.Code_str[49][7][4] = 0;	this.Code_str[49][8][4] = 0;	this.Code_str[49][9][4] = 0;		
		this.Code_str[49][0][5] = 0;	this.Code_str[49][1][5] = 0;	this.Code_str[49][2][5] = 0;	this.Code_str[49][3][5] = 0;	this.Code_str[49][4][5] = 0;	this.Code_str[49][5][5] = 1;	this.Code_str[49][6][5] = 0;	this.Code_str[49][7][5] = 0;	this.Code_str[49][8][5] = 0;	this.Code_str[49][9][5] = 0;		
		this.Code_str[49][0][6] = 0;	this.Code_str[49][1][6] = 0;	this.Code_str[49][2][6] = 0;	this.Code_str[49][3][6] = 0;	this.Code_str[49][4][6] = 0;	this.Code_str[49][5][6] = 1;	this.Code_str[49][6][6] = 0;	this.Code_str[49][7][6] = 0;	this.Code_str[49][8][6] = 0;	this.Code_str[49][9][6] = 0;		
		this.Code_str[49][0][7] = 0;	this.Code_str[49][1][7] = 0;	this.Code_str[49][2][7] = 0;	this.Code_str[49][3][7] = 0;	this.Code_str[49][4][7] = 0;	this.Code_str[49][5][7] = 1;	this.Code_str[49][6][7] = 0;	this.Code_str[49][7][7] = 0;	this.Code_str[49][8][7] = 0;	this.Code_str[49][9][7] = 0;		
		this.Code_str[49][0][8] = 0;	this.Code_str[49][1][8] = 0;	this.Code_str[49][2][8] = 0;	this.Code_str[49][3][8] = 0;	this.Code_str[49][4][8] = 0;	this.Code_str[49][5][8] = 1;	this.Code_str[49][6][8] = 0;	this.Code_str[49][7][8] = 0;	this.Code_str[49][8][8] = 0;	this.Code_str[49][9][8] = 0;		
		this.Code_str[49][0][9] = 0;	this.Code_str[49][1][9] = 0;	this.Code_str[49][2][9] = 0;	this.Code_str[49][3][9] = 0;	this.Code_str[49][4][9] = 0;	this.Code_str[49][5][9] = 1;	this.Code_str[49][6][9] = 0;	this.Code_str[49][7][9] = 0;	this.Code_str[49][8][9] = 0;	this.Code_str[49][9][9] = 0;		
		
		// Символ "2"
		this.Code_str[50][0][0] = 0;	this.Code_str[50][1][0] = 0;	this.Code_str[50][2][0] = 0;	this.Code_str[50][3][0] = 0;	this.Code_str[50][4][0] = 1;	this.Code_str[50][5][0] = 1;	this.Code_str[50][6][0] = 1;	this.Code_str[50][7][0] = 0;	this.Code_str[50][8][0] = 0;	this.Code_str[50][9][0] = 0;		
		this.Code_str[50][0][1] = 0;	this.Code_str[50][1][1] = 0;	this.Code_str[50][2][1] = 0;	this.Code_str[50][3][1] = 1;	this.Code_str[50][4][1] = 0;	this.Code_str[50][5][1] = 0;	this.Code_str[50][6][1] = 0;	this.Code_str[50][7][1] = 1;	this.Code_str[50][8][1] = 0;	this.Code_str[50][9][1] = 0;		
		this.Code_str[50][0][2] = 0;	this.Code_str[50][1][2] = 0;	this.Code_str[50][2][2] = 0;	this.Code_str[50][3][2] = 1;	this.Code_str[50][4][2] = 0;	this.Code_str[50][5][2] = 0;	this.Code_str[50][6][2] = 0;	this.Code_str[50][7][2] = 1;	this.Code_str[50][8][2] = 0;	this.Code_str[50][9][2] = 0;		
		this.Code_str[50][0][3] = 0;	this.Code_str[50][1][3] = 0;	this.Code_str[50][2][3] = 0;	this.Code_str[50][3][3] = 0;	this.Code_str[50][4][3] = 1;	this.Code_str[50][5][3] = 0;	this.Code_str[50][6][3] = 0;	this.Code_str[50][7][3] = 1;	this.Code_str[50][8][3] = 0;	this.Code_str[50][9][3] = 0;		
		this.Code_str[50][0][4] = 0;	this.Code_str[50][1][4] = 0;	this.Code_str[50][2][4] = 0;	this.Code_str[50][3][4] = 0;	this.Code_str[50][4][4] = 0;	this.Code_str[50][5][4] = 0;	this.Code_str[50][6][4] = 1;	this.Code_str[50][7][4] = 0;	this.Code_str[50][8][4] = 0;	this.Code_str[50][9][4] = 0;		
		this.Code_str[50][0][5] = 0;	this.Code_str[50][1][5] = 0;	this.Code_str[50][2][5] = 0;	this.Code_str[50][3][5] = 0;	this.Code_str[50][4][5] = 0;	this.Code_str[50][5][5] = 1;	this.Code_str[50][6][5] = 0;	this.Code_str[50][7][5] = 0;	this.Code_str[50][8][5] = 0;	this.Code_str[50][9][5] = 0;		
		this.Code_str[50][0][6] = 0;	this.Code_str[50][1][6] = 0;	this.Code_str[50][2][6] = 0;	this.Code_str[50][3][6] = 0;	this.Code_str[50][4][6] = 1;	this.Code_str[50][5][6] = 0;	this.Code_str[50][6][6] = 0;	this.Code_str[50][7][6] = 0;	this.Code_str[50][8][6] = 0;	this.Code_str[50][9][6] = 0;		
		this.Code_str[50][0][7] = 0;	this.Code_str[50][1][7] = 0;	this.Code_str[50][2][7] = 0;	this.Code_str[50][3][7] = 1;	this.Code_str[50][4][7] = 0;	this.Code_str[50][5][7] = 0;	this.Code_str[50][6][7] = 0;	this.Code_str[50][7][7] = 0;	this.Code_str[50][8][7] = 0;	this.Code_str[50][9][7] = 0;		
		this.Code_str[50][0][8] = 0;	this.Code_str[50][1][8] = 0;	this.Code_str[50][2][8] = 1;	this.Code_str[50][3][8] = 0;	this.Code_str[50][4][8] = 0;	this.Code_str[50][5][8] = 0;	this.Code_str[50][6][8] = 0;	this.Code_str[50][7][8] = 0;	this.Code_str[50][8][8] = 0;	this.Code_str[50][9][8] = 0;		
		this.Code_str[50][0][9] = 0;	this.Code_str[50][1][9] = 0;	this.Code_str[50][2][9] = 1;	this.Code_str[50][3][9] = 1;	this.Code_str[50][4][9] = 1;	this.Code_str[50][5][9] = 1;	this.Code_str[50][6][9] = 1;	this.Code_str[50][7][9] = 1;	this.Code_str[50][8][9] = 0;	this.Code_str[50][9][9] = 0;		
		
		// Символ "3" 
		this.Code_str[51][0][0] = 0;	this.Code_str[51][1][0] = 0;	this.Code_str[51][2][0] = 0;	this.Code_str[51][3][0] = 1;	this.Code_str[51][4][0] = 1;	this.Code_str[51][5][0] = 1;	this.Code_str[51][6][0] = 0;	this.Code_str[51][7][0] = 0;	this.Code_str[51][8][0] = 0;	this.Code_str[51][9][0] = 0;		
		this.Code_str[51][0][1] = 0;	this.Code_str[51][1][1] = 0;	this.Code_str[51][2][1] = 1;	this.Code_str[51][3][1] = 0;	this.Code_str[51][4][1] = 0;	this.Code_str[51][5][1] = 0;	this.Code_str[51][6][1] = 1;	this.Code_str[51][7][1] = 0;	this.Code_str[51][8][1] = 0;	this.Code_str[51][9][1] = 0;		
		this.Code_str[51][0][2] = 0;	this.Code_str[51][1][2] = 0;	this.Code_str[51][2][2] = 0;	this.Code_str[51][3][2] = 0;	this.Code_str[51][4][2] = 0;	this.Code_str[51][5][2] = 0;	this.Code_str[51][6][2] = 0;	this.Code_str[51][7][2] = 1;	this.Code_str[51][8][2] = 0;	this.Code_str[51][9][2] = 0;		
		this.Code_str[51][0][3] = 0;	this.Code_str[51][1][3] = 0;	this.Code_str[51][2][3] = 0;	this.Code_str[51][3][3] = 0;	this.Code_str[51][4][3] = 0;	this.Code_str[51][5][3] = 0;	this.Code_str[51][6][3] = 1;	this.Code_str[51][7][3] = 0;	this.Code_str[51][8][3] = 0;	this.Code_str[51][9][3] = 0;		
		this.Code_str[51][0][4] = 0;	this.Code_str[51][1][4] = 0;	this.Code_str[51][2][4] = 0;	this.Code_str[51][3][4] = 0;	this.Code_str[51][4][4] = 1;	this.Code_str[51][5][4] = 1;	this.Code_str[51][6][4] = 0;	this.Code_str[51][7][4] = 0;	this.Code_str[51][8][4] = 0;	this.Code_str[51][9][4] = 0;		
		this.Code_str[51][0][5] = 0;	this.Code_str[51][1][5] = 0;	this.Code_str[51][2][5] = 0;	this.Code_str[51][3][5] = 0;	this.Code_str[51][4][5] = 0;	this.Code_str[51][5][5] = 0;	this.Code_str[51][6][5] = 1;	this.Code_str[51][7][5] = 0;	this.Code_str[51][8][5] = 0;	this.Code_str[51][9][5] = 0;		
		this.Code_str[51][0][6] = 0;	this.Code_str[51][1][6] = 0;	this.Code_str[51][2][6] = 0;	this.Code_str[51][3][6] = 0;	this.Code_str[51][4][6] = 0;	this.Code_str[51][5][6] = 0;	this.Code_str[51][6][6] = 0;	this.Code_str[51][7][6] = 1;	this.Code_str[51][8][6] = 0;	this.Code_str[51][9][6] = 0;		
		this.Code_str[51][0][7] = 0;	this.Code_str[51][1][7] = 0;	this.Code_str[51][2][7] = 0;	this.Code_str[51][3][7] = 0;	this.Code_str[51][4][7] = 0;	this.Code_str[51][5][7] = 0;	this.Code_str[51][6][7] = 0;	this.Code_str[51][7][7] = 1;	this.Code_str[51][8][7] = 0;	this.Code_str[51][9][7] = 0;		
		this.Code_str[51][0][8] = 0;	this.Code_str[51][1][8] = 0;	this.Code_str[51][2][8] = 1;	this.Code_str[51][3][8] = 0;	this.Code_str[51][4][8] = 0;	this.Code_str[51][5][8] = 0;	this.Code_str[51][6][8] = 1;	this.Code_str[51][7][8] = 0;	this.Code_str[51][8][8] = 0;	this.Code_str[51][9][8] = 0;		
		this.Code_str[51][0][9] = 0;	this.Code_str[51][1][9] = 0;	this.Code_str[51][2][9] = 0;	this.Code_str[51][3][9] = 1;	this.Code_str[51][4][9] = 1;	this.Code_str[51][5][9] = 1;	this.Code_str[51][6][9] = 0;	this.Code_str[51][7][9] = 0;	this.Code_str[51][8][9] = 0;	this.Code_str[51][9][9] = 0;		

		// Символ "4"
		this.Code_str[52][0][0] = 0;	this.Code_str[52][1][0] = 0;	this.Code_str[52][2][0] = 0;	this.Code_str[52][3][0] = 0;	this.Code_str[52][4][0] = 1;	this.Code_str[52][5][0] = 0;	this.Code_str[52][6][0] = 0;	this.Code_str[52][7][0] = 1;	this.Code_str[52][8][0] = 0;	this.Code_str[52][9][0] = 0;		
		this.Code_str[52][0][1] = 0;	this.Code_str[52][1][1] = 0;	this.Code_str[52][2][1] = 0;	this.Code_str[52][3][1] = 0;	this.Code_str[52][4][1] = 1;	this.Code_str[52][5][1] = 0;	this.Code_str[52][6][1] = 0;	this.Code_str[52][7][1] = 1;	this.Code_str[52][8][1] = 0;	this.Code_str[52][9][1] = 0;		
		this.Code_str[52][0][2] = 0;	this.Code_str[52][1][2] = 0;	this.Code_str[52][2][2] = 0;	this.Code_str[52][3][2] = 1;	this.Code_str[52][4][2] = 0;	this.Code_str[52][5][2] = 0;	this.Code_str[52][6][2] = 0;	this.Code_str[52][7][2] = 1;	this.Code_str[52][8][2] = 0;	this.Code_str[52][9][2] = 0;		
		this.Code_str[52][0][3] = 0;	this.Code_str[52][1][3] = 0;	this.Code_str[52][2][3] = 0;	this.Code_str[52][3][3] = 1;	this.Code_str[52][4][3] = 0;	this.Code_str[52][5][3] = 0;	this.Code_str[52][6][3] = 0;	this.Code_str[52][7][3] = 1;	this.Code_str[52][8][3] = 0;	this.Code_str[52][9][3] = 0;		
		this.Code_str[52][0][4] = 0;	this.Code_str[52][1][4] = 0;	this.Code_str[52][2][4] = 0;	this.Code_str[52][3][4] = 1;	this.Code_str[52][4][4] = 1;	this.Code_str[52][5][4] = 1;	this.Code_str[52][6][4] = 1;	this.Code_str[52][7][4] = 1;	this.Code_str[52][8][4] = 0;	this.Code_str[52][9][4] = 0;		
		this.Code_str[52][0][5] = 0;	this.Code_str[52][1][5] = 0;	this.Code_str[52][2][5] = 0;	this.Code_str[52][3][5] = 0;	this.Code_str[52][4][5] = 0;	this.Code_str[52][5][5] = 0;	this.Code_str[52][6][5] = 0;	this.Code_str[52][7][5] = 1;	this.Code_str[52][8][5] = 0;	this.Code_str[52][9][5] = 0;		
		this.Code_str[52][0][6] = 0;	this.Code_str[52][1][6] = 0;	this.Code_str[52][2][6] = 0;	this.Code_str[52][3][6] = 0;	this.Code_str[52][4][6] = 0;	this.Code_str[52][5][6] = 0;	this.Code_str[52][6][6] = 0;	this.Code_str[52][7][6] = 1;	this.Code_str[52][8][6] = 0;	this.Code_str[52][9][6] = 0;		
		this.Code_str[52][0][7] = 0;	this.Code_str[52][1][7] = 0;	this.Code_str[52][2][7] = 0;	this.Code_str[52][3][7] = 0;	this.Code_str[52][4][7] = 0;	this.Code_str[52][5][7] = 0;	this.Code_str[52][6][7] = 0;	this.Code_str[52][7][7] = 1;	this.Code_str[52][8][7] = 0;	this.Code_str[52][9][7] = 0;		
		this.Code_str[52][0][8] = 0;	this.Code_str[52][1][8] = 0;	this.Code_str[52][2][8] = 0;	this.Code_str[52][3][8] = 0;	this.Code_str[52][4][8] = 0;	this.Code_str[52][5][8] = 0;	this.Code_str[52][6][8] = 0;	this.Code_str[52][7][8] = 1;	this.Code_str[52][8][8] = 0;	this.Code_str[52][9][8] = 0;		
		this.Code_str[52][0][9] = 0;	this.Code_str[52][1][9] = 0;	this.Code_str[52][2][9] = 0;	this.Code_str[52][3][9] = 0;	this.Code_str[52][4][9] = 0;	this.Code_str[52][5][9] = 0;	this.Code_str[52][6][9] = 0;	this.Code_str[52][7][9] = 1;	this.Code_str[52][8][9] = 0;	this.Code_str[52][9][9] = 0;		
		
		// Символ "5"
		this.Code_str[53][0][0] = 0;	this.Code_str[53][1][0] = 0;	this.Code_str[53][2][0] = 1;	this.Code_str[53][3][0] = 1;	this.Code_str[53][4][0] = 1;	this.Code_str[53][5][0] = 1;	this.Code_str[53][6][0] = 1;	this.Code_str[53][7][0] = 1;	this.Code_str[53][8][0] = 0;	this.Code_str[53][9][0] = 0;		
		this.Code_str[53][0][1] = 0;	this.Code_str[53][1][1] = 0;	this.Code_str[53][2][1] = 1;	this.Code_str[53][3][1] = 0;	this.Code_str[53][4][1] = 0;	this.Code_str[53][5][1] = 0;	this.Code_str[53][6][1] = 0;	this.Code_str[53][7][1] = 0;	this.Code_str[53][8][1] = 0;	this.Code_str[53][9][1] = 0;		
		this.Code_str[53][0][2] = 0;	this.Code_str[53][1][2] = 0;	this.Code_str[53][2][2] = 1;	this.Code_str[53][3][2] = 0;	this.Code_str[53][4][2] = 0;	this.Code_str[53][5][2] = 0;	this.Code_str[53][6][2] = 0;	this.Code_str[53][7][2] = 0;	this.Code_str[53][8][2] = 0;	this.Code_str[53][9][2] = 0;		
		this.Code_str[53][0][3] = 0;	this.Code_str[53][1][3] = 0;	this.Code_str[53][2][3] = 1;	this.Code_str[53][3][3] = 0;	this.Code_str[53][4][3] = 0;	this.Code_str[53][5][3] = 0;	this.Code_str[53][6][3] = 0;	this.Code_str[53][7][3] = 0;	this.Code_str[53][8][3] = 0;	this.Code_str[53][9][3] = 0;		
		this.Code_str[53][0][4] = 0;	this.Code_str[53][1][4] = 0;	this.Code_str[53][2][4] = 1;	this.Code_str[53][3][4] = 1;	this.Code_str[53][4][4] = 1;	this.Code_str[53][5][4] = 1;	this.Code_str[53][6][4] = 1;	this.Code_str[53][7][4] = 0;	this.Code_str[53][8][4] = 0;	this.Code_str[53][9][4] = 0;		
		this.Code_str[53][0][5] = 0;	this.Code_str[53][1][5] = 0;	this.Code_str[53][2][5] = 0;	this.Code_str[53][3][5] = 0;	this.Code_str[53][4][5] = 0;	this.Code_str[53][5][5] = 0;	this.Code_str[53][6][5] = 0;	this.Code_str[53][7][5] = 1;	this.Code_str[53][8][5] = 0;	this.Code_str[53][9][5] = 0;		
		this.Code_str[53][0][6] = 0;	this.Code_str[53][1][6] = 0;	this.Code_str[53][2][6] = 0;	this.Code_str[53][3][6] = 0;	this.Code_str[53][4][6] = 0;	this.Code_str[53][5][6] = 0;	this.Code_str[53][6][6] = 0;	this.Code_str[53][7][6] = 1;	this.Code_str[53][8][6] = 0;	this.Code_str[53][9][6] = 0;		
		this.Code_str[53][0][7] = 0;	this.Code_str[53][1][7] = 0;	this.Code_str[53][2][7] = 0;	this.Code_str[53][3][7] = 0;	this.Code_str[53][4][7] = 0;	this.Code_str[53][5][7] = 0;	this.Code_str[53][6][7] = 0;	this.Code_str[53][7][7] = 1;	this.Code_str[53][8][7] = 0;	this.Code_str[53][9][7] = 0;		
		this.Code_str[53][0][8] = 0;	this.Code_str[53][1][8] = 0;	this.Code_str[53][2][8] = 1;	this.Code_str[53][3][8] = 0;	this.Code_str[53][4][8] = 0;	this.Code_str[53][5][8] = 0;	this.Code_str[53][6][8] = 1;	this.Code_str[53][7][8] = 0;	this.Code_str[53][8][8] = 0;	this.Code_str[53][9][8] = 0;		
		this.Code_str[53][0][9] = 0;	this.Code_str[53][1][9] = 0;	this.Code_str[53][2][9] = 0;	this.Code_str[53][3][9] = 1;	this.Code_str[53][4][9] = 1;	this.Code_str[53][5][9] = 1;	this.Code_str[53][6][9] = 0;	this.Code_str[53][7][9] = 0;	this.Code_str[53][8][9] = 0;	this.Code_str[53][9][9] = 0;		
		
		// Символ "6"
		this.Code_str[54][0][0] = 0;	this.Code_str[54][1][0] = 0;	this.Code_str[54][2][0] = 0;	this.Code_str[54][3][0] = 0;	this.Code_str[54][4][0] = 1;	this.Code_str[54][5][0] = 1;	this.Code_str[54][6][0] = 1;	this.Code_str[54][7][0] = 0;	this.Code_str[54][8][0] = 0;	this.Code_str[54][9][0] = 0;		
		this.Code_str[54][0][1] = 0;	this.Code_str[54][1][1] = 0;	this.Code_str[54][2][1] = 0;	this.Code_str[54][3][1] = 1;	this.Code_str[54][4][1] = 0;	this.Code_str[54][5][1] = 0;	this.Code_str[54][6][1] = 0;	this.Code_str[54][7][1] = 1;	this.Code_str[54][8][1] = 0;	this.Code_str[54][9][1] = 0;		
		this.Code_str[54][0][2] = 0;	this.Code_str[54][1][2] = 0;	this.Code_str[54][2][2] = 0;	this.Code_str[54][3][2] = 1;	this.Code_str[54][4][2] = 0;	this.Code_str[54][5][2] = 0;	this.Code_str[54][6][2] = 0;	this.Code_str[54][7][2] = 0;	this.Code_str[54][8][2] = 0;	this.Code_str[54][9][2] = 0;		
		this.Code_str[54][0][3] = 0;	this.Code_str[54][1][3] = 0;	this.Code_str[54][2][3] = 0;	this.Code_str[54][3][3] = 1;	this.Code_str[54][4][3] = 0;	this.Code_str[54][5][3] = 0;	this.Code_str[54][6][3] = 0;	this.Code_str[54][7][3] = 0;	this.Code_str[54][8][3] = 0;	this.Code_str[54][9][3] = 0;		
		this.Code_str[54][0][4] = 0;	this.Code_str[54][1][4] = 0;	this.Code_str[54][2][4] = 0;	this.Code_str[54][3][4] = 1;	this.Code_str[54][4][4] = 1;	this.Code_str[54][5][4] = 1;	this.Code_str[54][6][4] = 1;	this.Code_str[54][7][4] = 0;	this.Code_str[54][8][4] = 0;	this.Code_str[54][9][4] = 0;		
		this.Code_str[54][0][5] = 0;	this.Code_str[54][1][5] = 0;	this.Code_str[54][2][5] = 0;	this.Code_str[54][3][5] = 1;	this.Code_str[54][4][5] = 0;	this.Code_str[54][5][5] = 0;	this.Code_str[54][6][5] = 0;	this.Code_str[54][7][5] = 1;	this.Code_str[54][8][5] = 0;	this.Code_str[54][9][5] = 0;		
		this.Code_str[54][0][6] = 0;	this.Code_str[54][1][6] = 0;	this.Code_str[54][2][6] = 0;	this.Code_str[54][3][6] = 1;	this.Code_str[54][4][6] = 0;	this.Code_str[54][5][6] = 0;	this.Code_str[54][6][6] = 0;	this.Code_str[54][7][6] = 1;	this.Code_str[54][8][6] = 0;	this.Code_str[54][9][6] = 0;		
		this.Code_str[54][0][7] = 0;	this.Code_str[54][1][7] = 0;	this.Code_str[54][2][7] = 0;	this.Code_str[54][3][7] = 1;	this.Code_str[54][4][7] = 0;	this.Code_str[54][5][7] = 0;	this.Code_str[54][6][7] = 0;	this.Code_str[54][7][7] = 1;	this.Code_str[54][8][7] = 0;	this.Code_str[54][9][7] = 0;		
		this.Code_str[54][0][8] = 0;	this.Code_str[54][1][8] = 0;	this.Code_str[54][2][8] = 0;	this.Code_str[54][3][8] = 1;	this.Code_str[54][4][8] = 0;	this.Code_str[54][5][8] = 0;	this.Code_str[54][6][8] = 0;	this.Code_str[54][7][8] = 1;	this.Code_str[54][8][8] = 0;	this.Code_str[54][9][8] = 0;		
		this.Code_str[54][0][9] = 0;	this.Code_str[54][1][9] = 0;	this.Code_str[54][2][9] = 0;	this.Code_str[54][3][9] = 0;	this.Code_str[54][4][9] = 1;	this.Code_str[54][5][9] = 1;	this.Code_str[54][6][9] = 1;	this.Code_str[54][7][9] = 0;	this.Code_str[54][8][9] = 0;	this.Code_str[54][9][9] = 0;		

		// Символ "7"  
		this.Code_str[55][0][0] = 0;	this.Code_str[55][1][0] = 1;	this.Code_str[55][2][0] = 1;	this.Code_str[55][3][0] = 1;	this.Code_str[55][4][0] = 1;	this.Code_str[55][5][0] = 1;	this.Code_str[55][6][0] = 1;	this.Code_str[55][7][0] = 1;	this.Code_str[55][8][0] = 1;	this.Code_str[55][9][0] = 0;		
		this.Code_str[55][0][1] = 0;	this.Code_str[55][1][1] = 0;	this.Code_str[55][2][1] = 0;	this.Code_str[55][3][1] = 0;	this.Code_str[55][4][1] = 0;	this.Code_str[55][5][1] = 0;	this.Code_str[55][6][1] = 0;	this.Code_str[55][7][1] = 0;	this.Code_str[55][8][1] = 1;	this.Code_str[55][9][1] = 0;		
		this.Code_str[55][0][2] = 0;	this.Code_str[55][1][2] = 0;	this.Code_str[55][2][2] = 0;	this.Code_str[55][3][2] = 0;	this.Code_str[55][4][2] = 0;	this.Code_str[55][5][2] = 0;	this.Code_str[55][6][2] = 0;	this.Code_str[55][7][2] = 1;	this.Code_str[55][8][2] = 0;	this.Code_str[55][9][2] = 0;		
		this.Code_str[55][0][3] = 0;	this.Code_str[55][1][3] = 0;	this.Code_str[55][2][3] = 0;	this.Code_str[55][3][3] = 0;	this.Code_str[55][4][3] = 0;	this.Code_str[55][5][3] = 0;	this.Code_str[55][6][3] = 0;	this.Code_str[55][7][3] = 1;	this.Code_str[55][8][3] = 0;	this.Code_str[55][9][3] = 0;		
		this.Code_str[55][0][4] = 0;	this.Code_str[55][1][4] = 0;	this.Code_str[55][2][4] = 0;	this.Code_str[55][3][4] = 0;	this.Code_str[55][4][4] = 0;	this.Code_str[55][5][4] = 0;	this.Code_str[55][6][4] = 1;	this.Code_str[55][7][4] = 0;	this.Code_str[55][8][4] = 0;	this.Code_str[55][9][4] = 0;		
		this.Code_str[55][0][5] = 0;	this.Code_str[55][1][5] = 0;	this.Code_str[55][2][5] = 0;	this.Code_str[55][3][5] = 0;	this.Code_str[55][4][5] = 0;	this.Code_str[55][5][5] = 0;	this.Code_str[55][6][5] = 1;	this.Code_str[55][7][5] = 0;	this.Code_str[55][8][5] = 0;	this.Code_str[55][9][5] = 0;		
		this.Code_str[55][0][6] = 0;	this.Code_str[55][1][6] = 0;	this.Code_str[55][2][6] = 0;	this.Code_str[55][3][6] = 0;	this.Code_str[55][4][6] = 0;	this.Code_str[55][5][6] = 0;	this.Code_str[55][6][6] = 1;	this.Code_str[55][7][6] = 0;	this.Code_str[55][8][6] = 0;	this.Code_str[55][9][6] = 0;		
		this.Code_str[55][0][7] = 0;	this.Code_str[55][1][7] = 0;	this.Code_str[55][2][7] = 0;	this.Code_str[55][3][7] = 0;	this.Code_str[55][4][7] = 0;	this.Code_str[55][5][7] = 1;	this.Code_str[55][6][7] = 0;	this.Code_str[55][7][7] = 0;	this.Code_str[55][8][7] = 0;	this.Code_str[55][9][7] = 0;		
		this.Code_str[55][0][8] = 0;	this.Code_str[55][1][8] = 0;	this.Code_str[55][2][8] = 0;	this.Code_str[55][3][8] = 0;	this.Code_str[55][4][8] = 0;	this.Code_str[55][5][8] = 1;	this.Code_str[55][6][8] = 0;	this.Code_str[55][7][8] = 0;	this.Code_str[55][8][8] = 0;	this.Code_str[55][9][8] = 0;		
		this.Code_str[55][0][9] = 0;	this.Code_str[55][1][9] = 0;	this.Code_str[55][2][9] = 0;	this.Code_str[55][3][9] = 0;	this.Code_str[55][4][9] = 0;	this.Code_str[55][5][9] = 1;	this.Code_str[55][6][9] = 0;	this.Code_str[55][7][9] = 0;	this.Code_str[55][8][9] = 0;	this.Code_str[55][9][9] = 0;		

		// Символ "8"  
		this.Code_str[56][0][0] = 0;	this.Code_str[56][1][0] = 0;	this.Code_str[56][2][0] = 0;	this.Code_str[56][3][0] = 1;	this.Code_str[56][4][0] = 1;	this.Code_str[56][5][0] = 0;	this.Code_str[56][6][0] = 0;	this.Code_str[56][7][0] = 0;	this.Code_str[56][8][0] = 0;	this.Code_str[56][9][0] = 0;		
		this.Code_str[56][0][1] = 0;	this.Code_str[56][1][1] = 0;	this.Code_str[56][2][1] = 1;	this.Code_str[56][3][1] = 0;	this.Code_str[56][4][1] = 0;	this.Code_str[56][5][1] = 1;	this.Code_str[56][6][1] = 0;	this.Code_str[56][7][1] = 0;	this.Code_str[56][8][1] = 0;	this.Code_str[56][9][1] = 0;		
		this.Code_str[56][0][2] = 0;	this.Code_str[56][1][2] = 1;	this.Code_str[56][2][2] = 0;	this.Code_str[56][3][2] = 0;	this.Code_str[56][4][2] = 0;	this.Code_str[56][5][2] = 0;	this.Code_str[56][6][2] = 1;	this.Code_str[56][7][2] = 0;	this.Code_str[56][8][2] = 0;	this.Code_str[56][9][2] = 0;		
		this.Code_str[56][0][3] = 0;	this.Code_str[56][1][3] = 1;	this.Code_str[56][2][3] = 0;	this.Code_str[56][3][3] = 0;	this.Code_str[56][4][3] = 0;	this.Code_str[56][5][3] = 0;	this.Code_str[56][6][3] = 1;	this.Code_str[56][7][3] = 0;	this.Code_str[56][8][3] = 0;	this.Code_str[56][9][3] = 0;		
		this.Code_str[56][0][4] = 0;	this.Code_str[56][1][4] = 0;	this.Code_str[56][2][4] = 1;	this.Code_str[56][3][4] = 0;	this.Code_str[56][4][4] = 0;	this.Code_str[56][5][4] = 1;	this.Code_str[56][6][4] = 0;	this.Code_str[56][7][4] = 0;	this.Code_str[56][8][4] = 0;	this.Code_str[56][9][4] = 0;		
		this.Code_str[56][0][5] = 0;	this.Code_str[56][1][5] = 0;	this.Code_str[56][2][5] = 0;	this.Code_str[56][3][5] = 1;	this.Code_str[56][4][5] = 1;	this.Code_str[56][5][5] = 0;	this.Code_str[56][6][5] = 0;	this.Code_str[56][7][5] = 0;	this.Code_str[56][8][5] = 0;	this.Code_str[56][9][5] = 0;		
		this.Code_str[56][0][6] = 0;	this.Code_str[56][1][6] = 0;	this.Code_str[56][2][6] = 1;	this.Code_str[56][3][6] = 0;	this.Code_str[56][4][6] = 0;	this.Code_str[56][5][6] = 1;	this.Code_str[56][6][6] = 0;	this.Code_str[56][7][6] = 0;	this.Code_str[56][8][6] = 0;	this.Code_str[56][9][6] = 0;		
		this.Code_str[56][0][7] = 0;	this.Code_str[56][1][7] = 1;	this.Code_str[56][2][7] = 0;	this.Code_str[56][3][7] = 0;	this.Code_str[56][4][7] = 0;	this.Code_str[56][5][7] = 0;	this.Code_str[56][6][7] = 1;	this.Code_str[56][7][7] = 0;	this.Code_str[56][8][7] = 0;	this.Code_str[56][9][7] = 0;		
		this.Code_str[56][0][8] = 0;	this.Code_str[56][1][8] = 1;	this.Code_str[56][2][8] = 0;	this.Code_str[56][3][8] = 0;	this.Code_str[56][4][8] = 0;	this.Code_str[56][5][8] = 0;	this.Code_str[56][6][8] = 1;	this.Code_str[56][7][8] = 0;	this.Code_str[56][8][8] = 0;	this.Code_str[56][9][8] = 0;		
		this.Code_str[56][0][9] = 0;	this.Code_str[56][1][9] = 0;	this.Code_str[56][2][9] = 1;	this.Code_str[56][3][9] = 1;	this.Code_str[56][4][9] = 1;	this.Code_str[56][5][9] = 1;	this.Code_str[56][6][9] = 0;	this.Code_str[56][7][9] = 0;	this.Code_str[56][8][9] = 0;	this.Code_str[56][9][9] = 0;		
		
		// Символ "9"  
		this.Code_str[57][0][0] = 0;	this.Code_str[57][1][0] = 0;	this.Code_str[57][2][0] = 0;	this.Code_str[57][3][0] = 1;	this.Code_str[57][4][0] = 1;	this.Code_str[57][5][0] = 1;	this.Code_str[57][6][0] = 0;	this.Code_str[57][7][0] = 0;	this.Code_str[57][8][0] = 0;	this.Code_str[57][9][0] = 0;		
		this.Code_str[57][0][1] = 0;	this.Code_str[57][1][1] = 0;	this.Code_str[57][2][1] = 1;	this.Code_str[57][3][1] = 0;	this.Code_str[57][4][1] = 0;	this.Code_str[57][5][1] = 0;	this.Code_str[57][6][1] = 1;	this.Code_str[57][7][1] = 0;	this.Code_str[57][8][1] = 0;	this.Code_str[57][9][1] = 0;		
		this.Code_str[57][0][2] = 0;	this.Code_str[57][1][2] = 1;	this.Code_str[57][2][2] = 0;	this.Code_str[57][3][2] = 0;	this.Code_str[57][4][2] = 0;	this.Code_str[57][5][2] = 0;	this.Code_str[57][6][2] = 0;	this.Code_str[57][7][2] = 1;	this.Code_str[57][8][2] = 0;	this.Code_str[57][9][2] = 0;		
		this.Code_str[57][0][3] = 0;	this.Code_str[57][1][3] = 1;	this.Code_str[57][2][3] = 0;	this.Code_str[57][3][3] = 0;	this.Code_str[57][4][3] = 0;	this.Code_str[57][5][3] = 0;	this.Code_str[57][6][3] = 0;	this.Code_str[57][7][3] = 1;	this.Code_str[57][8][3] = 0;	this.Code_str[57][9][3] = 0;		
		this.Code_str[57][0][4] = 0;	this.Code_str[57][1][4] = 0;	this.Code_str[57][2][4] = 1;	this.Code_str[57][3][4] = 0;	this.Code_str[57][4][4] = 0;	this.Code_str[57][5][4] = 0;	this.Code_str[57][6][4] = 1;	this.Code_str[57][7][4] = 1;	this.Code_str[57][8][4] = 0;	this.Code_str[57][9][4] = 0;		
		this.Code_str[57][0][5] = 0;	this.Code_str[57][1][5] = 0;	this.Code_str[57][2][5] = 0;	this.Code_str[57][3][5] = 1;	this.Code_str[57][4][5] = 1;	this.Code_str[57][5][5] = 1;	this.Code_str[57][6][5] = 0;	this.Code_str[57][7][5] = 1;	this.Code_str[57][8][5] = 0;	this.Code_str[57][9][5] = 0;		
		this.Code_str[57][0][6] = 0;	this.Code_str[57][1][6] = 0;	this.Code_str[57][2][6] = 0;	this.Code_str[57][3][6] = 0;	this.Code_str[57][4][6] = 0;	this.Code_str[57][5][6] = 0;	this.Code_str[57][6][6] = 0;	this.Code_str[57][7][6] = 1;	this.Code_str[57][8][6] = 0;	this.Code_str[57][9][6] = 0;		
		this.Code_str[57][0][7] = 0;	this.Code_str[57][1][7] = 0;	this.Code_str[57][2][7] = 0;	this.Code_str[57][3][7] = 0;	this.Code_str[57][4][7] = 0;	this.Code_str[57][5][7] = 0;	this.Code_str[57][6][7] = 0;	this.Code_str[57][7][7] = 1;	this.Code_str[57][8][7] = 0;	this.Code_str[57][9][7] = 0;		
		this.Code_str[57][0][8] = 0;	this.Code_str[57][1][8] = 0;	this.Code_str[57][2][8] = 1;	this.Code_str[57][3][8] = 0;	this.Code_str[57][4][8] = 0;	this.Code_str[57][5][8] = 0;	this.Code_str[57][6][8] = 1;	this.Code_str[57][7][8] = 0;	this.Code_str[57][8][8] = 0;	this.Code_str[57][9][8] = 0;		
		this.Code_str[57][0][9] = 0;	this.Code_str[57][1][9] = 0;	this.Code_str[57][2][9] = 0;	this.Code_str[57][3][9] = 1;	this.Code_str[57][4][9] = 1;	this.Code_str[57][5][9] = 1;	this.Code_str[57][6][9] = 0;	this.Code_str[57][7][9] = 0;	this.Code_str[57][8][9] = 0;	this.Code_str[57][9][9] = 0;		

		// Символ "A"  		
		this.Code_str[65][0][0] = 0;	this.Code_str[65][1][0] = 0;	this.Code_str[65][2][0] = 0;	this.Code_str[65][3][0] = 0;	this.Code_str[65][4][0] = 1;	this.Code_str[65][5][0] = 0;	this.Code_str[65][6][0] = 0;	this.Code_str[65][7][0] = 0;	this.Code_str[65][8][0] = 0;	this.Code_str[65][9][0] = 0;		
		this.Code_str[65][0][1] = 0;	this.Code_str[65][1][1] = 0;	this.Code_str[65][2][1] = 0;	this.Code_str[65][3][1] = 1;	this.Code_str[65][4][1] = 0;	this.Code_str[65][5][1] = 1;	this.Code_str[65][6][1] = 0;	this.Code_str[65][7][1] = 0;	this.Code_str[65][8][1] = 0;	this.Code_str[65][9][1] = 0;		
		this.Code_str[65][0][2] = 0;	this.Code_str[65][1][2] = 0;	this.Code_str[65][2][2] = 0;	this.Code_str[65][3][2] = 1;	this.Code_str[65][4][2] = 0;	this.Code_str[65][5][2] = 1;	this.Code_str[65][6][2] = 0;	this.Code_str[65][7][2] = 0;	this.Code_str[65][8][2] = 0;	this.Code_str[65][9][2] = 0;		
		this.Code_str[65][0][3] = 0;	this.Code_str[65][1][3] = 0;	this.Code_str[65][2][3] = 1;	this.Code_str[65][3][3] = 0;	this.Code_str[65][4][3] = 0;	this.Code_str[65][5][3] = 0;	this.Code_str[65][6][3] = 1;	this.Code_str[65][7][3] = 0;	this.Code_str[65][8][3] = 0;	this.Code_str[65][9][3] = 0;		
		this.Code_str[65][0][4] = 0;	this.Code_str[65][1][4] = 0;	this.Code_str[65][2][4] = 1;	this.Code_str[65][3][4] = 0;	this.Code_str[65][4][4] = 0;	this.Code_str[65][5][4] = 0;	this.Code_str[65][6][4] = 1;	this.Code_str[65][7][4] = 0;	this.Code_str[65][8][4] = 0;	this.Code_str[65][9][4] = 0;		
		this.Code_str[65][0][5] = 0;	this.Code_str[65][1][5] = 0;	this.Code_str[65][2][5] = 1;	this.Code_str[65][3][5] = 1;	this.Code_str[65][4][5] = 1;	this.Code_str[65][5][5] = 1;	this.Code_str[65][6][5] = 1;	this.Code_str[65][7][5] = 0;	this.Code_str[65][8][5] = 0;	this.Code_str[65][9][5] = 0;		
		this.Code_str[65][0][6] = 0;	this.Code_str[65][1][6] = 1;	this.Code_str[65][2][6] = 0;	this.Code_str[65][3][6] = 0;	this.Code_str[65][4][6] = 0;	this.Code_str[65][5][6] = 0;	this.Code_str[65][6][6] = 0;	this.Code_str[65][7][6] = 1;	this.Code_str[65][8][6] = 0;	this.Code_str[65][9][6] = 0;		
		this.Code_str[65][0][7] = 0;	this.Code_str[65][1][7] = 1;	this.Code_str[65][2][7] = 0;	this.Code_str[65][3][7] = 0;	this.Code_str[65][4][7] = 0;	this.Code_str[65][5][7] = 0;	this.Code_str[65][6][7] = 0;	this.Code_str[65][7][7] = 1;	this.Code_str[65][8][7] = 0;	this.Code_str[65][9][7] = 0;		
		this.Code_str[65][0][8] = 1;	this.Code_str[65][1][8] = 0;	this.Code_str[65][2][8] = 0;	this.Code_str[65][3][8] = 0;	this.Code_str[65][4][8] = 0;	this.Code_str[65][5][8] = 0;	this.Code_str[65][6][8] = 0;	this.Code_str[65][7][8] = 0;	this.Code_str[65][8][8] = 1;	this.Code_str[65][9][8] = 0;		
		this.Code_str[65][0][9] = 1;	this.Code_str[65][1][9] = 0;	this.Code_str[65][2][9] = 0;	this.Code_str[65][3][9] = 0;	this.Code_str[65][4][9] = 0;	this.Code_str[65][5][9] = 0;	this.Code_str[65][6][9] = 0;	this.Code_str[65][7][9] = 0;	this.Code_str[65][8][9] = 1;	this.Code_str[65][9][9] = 0;		

		// Символ "C" 	
		this.Code_str[67][0][0] = 0;	this.Code_str[67][1][0] = 0;	this.Code_str[67][2][0] = 0;	this.Code_str[67][3][0] = 1;	this.Code_str[67][4][0] = 1;	this.Code_str[67][5][0] = 1;	this.Code_str[67][6][0] = 1;	this.Code_str[67][7][0] = 0;	this.Code_str[67][8][0] = 0;	this.Code_str[67][9][0] = 0;		
		this.Code_str[67][0][1] = 0;	this.Code_str[67][1][1] = 0;	this.Code_str[67][2][1] = 1;	this.Code_str[67][3][1] = 0;	this.Code_str[67][4][1] = 0;	this.Code_str[67][5][1] = 0;	this.Code_str[67][6][1] = 0;	this.Code_str[67][7][1] = 1;	this.Code_str[67][8][1] = 0;	this.Code_str[67][9][1] = 0;		
		this.Code_str[67][0][2] = 0;	this.Code_str[67][1][2] = 1;	this.Code_str[67][2][2] = 0;	this.Code_str[67][3][2] = 0;	this.Code_str[67][4][2] = 0;	this.Code_str[67][5][2] = 0;	this.Code_str[67][6][2] = 0;	this.Code_str[67][7][2] = 0;	this.Code_str[67][8][2] = 0;	this.Code_str[67][9][2] = 0;		
		this.Code_str[67][0][3] = 0;	this.Code_str[67][1][3] = 1;	this.Code_str[67][2][3] = 0;	this.Code_str[67][3][3] = 0;	this.Code_str[67][4][3] = 0;	this.Code_str[67][5][3] = 0;	this.Code_str[67][6][3] = 0;	this.Code_str[67][7][3] = 0;	this.Code_str[67][8][3] = 0;	this.Code_str[67][9][3] = 0;		
		this.Code_str[67][0][4] = 0;	this.Code_str[67][1][4] = 1;	this.Code_str[67][2][4] = 0;	this.Code_str[67][3][4] = 0;	this.Code_str[67][4][4] = 0;	this.Code_str[67][5][4] = 0;	this.Code_str[67][6][4] = 0;	this.Code_str[67][7][4] = 0;	this.Code_str[67][8][4] = 0;	this.Code_str[67][9][4] = 0;		
		this.Code_str[67][0][5] = 0;	this.Code_str[67][1][5] = 1;	this.Code_str[67][2][5] = 0;	this.Code_str[67][3][5] = 0;	this.Code_str[67][4][5] = 0;	this.Code_str[67][5][5] = 0;	this.Code_str[67][6][5] = 0;	this.Code_str[67][7][5] = 0;	this.Code_str[67][8][5] = 0;	this.Code_str[67][9][5] = 0;		
		this.Code_str[67][0][6] = 0;	this.Code_str[67][1][6] = 1;	this.Code_str[67][2][6] = 0;	this.Code_str[67][3][6] = 0;	this.Code_str[67][4][6] = 0;	this.Code_str[67][5][6] = 0;	this.Code_str[67][6][6] = 0;	this.Code_str[67][7][6] = 0;	this.Code_str[67][8][6] = 0;	this.Code_str[67][9][6] = 0;		
		this.Code_str[67][0][7] = 0;	this.Code_str[67][1][7] = 1;	this.Code_str[67][2][7] = 0;	this.Code_str[67][3][7] = 0;	this.Code_str[67][4][7] = 0;	this.Code_str[67][5][7] = 0;	this.Code_str[67][6][7] = 0;	this.Code_str[67][7][7] = 0;	this.Code_str[67][8][7] = 0;	this.Code_str[67][9][7] = 0;		
		this.Code_str[67][0][8] = 0;	this.Code_str[67][1][8] = 0;	this.Code_str[67][2][8] = 1;	this.Code_str[67][3][8] = 0;	this.Code_str[67][4][8] = 0;	this.Code_str[67][5][8] = 0;	this.Code_str[67][6][8] = 0;	this.Code_str[67][7][8] = 1;	this.Code_str[67][8][8] = 0;	this.Code_str[67][9][8] = 0;		
		this.Code_str[67][0][9] = 0;	this.Code_str[67][1][9] = 0;	this.Code_str[67][2][9] = 0;	this.Code_str[67][3][9] = 1;	this.Code_str[67][4][9] = 1;	this.Code_str[67][5][9] = 1;	this.Code_str[67][6][9] = 1;	this.Code_str[67][7][9] = 0;	this.Code_str[67][8][9] = 0;	this.Code_str[67][9][9] = 0;		

		// Символ "K"
		this.Code_str[75][0][0] = 0;	this.Code_str[75][1][0] = 1;	this.Code_str[75][2][0] = 0;	this.Code_str[75][3][0] = 0;	this.Code_str[75][4][0] = 0;	this.Code_str[75][5][0] = 0;	this.Code_str[75][6][0] = 1;	this.Code_str[75][7][0] = 0;	this.Code_str[75][8][0] = 0;	this.Code_str[75][9][0] = 0;		
		this.Code_str[75][0][1] = 0;	this.Code_str[75][1][1] = 1;	this.Code_str[75][2][1] = 0;	this.Code_str[75][3][1] = 0;	this.Code_str[75][4][1] = 0;	this.Code_str[75][5][1] = 1;	this.Code_str[75][6][1] = 0;	this.Code_str[75][7][1] = 0;	this.Code_str[75][8][1] = 0;	this.Code_str[75][9][1] = 0;		
		this.Code_str[75][0][2] = 0;	this.Code_str[75][1][2] = 1;	this.Code_str[75][2][2] = 0;	this.Code_str[75][3][2] = 0;	this.Code_str[75][4][2] = 1;	this.Code_str[75][5][2] = 0;	this.Code_str[75][6][2] = 0;	this.Code_str[75][7][2] = 0;	this.Code_str[75][8][2] = 0;	this.Code_str[75][9][2] = 0;		
		this.Code_str[75][0][3] = 0;	this.Code_str[75][1][3] = 1;	this.Code_str[75][2][3] = 0;	this.Code_str[75][3][3] = 1;	this.Code_str[75][4][3] = 0;	this.Code_str[75][5][3] = 0;	this.Code_str[75][6][3] = 0;	this.Code_str[75][7][3] = 0;	this.Code_str[75][8][3] = 0;	this.Code_str[75][9][3] = 0;		
		this.Code_str[75][0][4] = 0;	this.Code_str[75][1][4] = 1;	this.Code_str[75][2][4] = 1;	this.Code_str[75][3][4] = 0;	this.Code_str[75][4][4] = 0;	this.Code_str[75][5][4] = 0;	this.Code_str[75][6][4] = 0;	this.Code_str[75][7][4] = 0;	this.Code_str[75][8][4] = 0;	this.Code_str[75][9][4] = 0;		
		this.Code_str[75][0][5] = 0;	this.Code_str[75][1][5] = 1;	this.Code_str[75][2][5] = 1;	this.Code_str[75][3][5] = 0;	this.Code_str[75][4][5] = 0;	this.Code_str[75][5][5] = 0;	this.Code_str[75][6][5] = 0;	this.Code_str[75][7][5] = 0;	this.Code_str[75][8][5] = 0;	this.Code_str[75][9][5] = 0;		
		this.Code_str[75][0][6] = 0;	this.Code_str[75][1][6] = 1;	this.Code_str[75][2][6] = 0;	this.Code_str[75][3][6] = 1;	this.Code_str[75][4][6] = 0;	this.Code_str[75][5][6] = 0;	this.Code_str[75][6][6] = 0;	this.Code_str[75][7][6] = 0;	this.Code_str[75][8][6] = 0;	this.Code_str[75][9][6] = 0;		
		this.Code_str[75][0][7] = 0;	this.Code_str[75][1][7] = 1;	this.Code_str[75][2][7] = 0;	this.Code_str[75][3][7] = 0;	this.Code_str[75][4][7] = 1;	this.Code_str[75][5][7] = 0;	this.Code_str[75][6][7] = 0;	this.Code_str[75][7][7] = 0;	this.Code_str[75][8][7] = 0;	this.Code_str[75][9][7] = 0;		
		this.Code_str[75][0][8] = 0;	this.Code_str[75][1][8] = 1;	this.Code_str[75][2][8] = 0;	this.Code_str[75][3][8] = 0;	this.Code_str[75][4][8] = 0;	this.Code_str[75][5][8] = 1;	this.Code_str[75][6][8] = 0;	this.Code_str[75][7][8] = 0;	this.Code_str[75][8][8] = 0;	this.Code_str[75][9][8] = 0;		
		this.Code_str[75][0][9] = 0;	this.Code_str[75][1][9] = 1;	this.Code_str[75][2][9] = 0;	this.Code_str[75][3][9] = 0;	this.Code_str[75][4][9] = 0;	this.Code_str[75][5][9] = 0;	this.Code_str[75][6][9] = 1;	this.Code_str[75][7][9] = 0;	this.Code_str[75][8][9] = 0;	this.Code_str[75][9][9] = 0;		

		// Символ "M" 
		this.Code_str[77][0][0] = 1;	this.Code_str[77][1][0] = 0;	this.Code_str[77][2][0] = 0;	this.Code_str[77][3][0] = 0;	this.Code_str[77][4][0] = 0;	this.Code_str[77][5][0] = 0;	this.Code_str[77][6][0] = 0;	this.Code_str[77][7][0] = 0;	this.Code_str[77][8][0] = 0;	this.Code_str[77][9][0] = 1;		
		this.Code_str[77][0][1] = 1;	this.Code_str[77][1][1] = 1;	this.Code_str[77][2][1] = 0;	this.Code_str[77][3][1] = 0;	this.Code_str[77][4][1] = 0;	this.Code_str[77][5][1] = 0;	this.Code_str[77][6][1] = 0;	this.Code_str[77][7][1] = 0;	this.Code_str[77][8][1] = 1;	this.Code_str[77][9][1] = 1;		
		this.Code_str[77][0][2] = 1;	this.Code_str[77][1][2] = 0;	this.Code_str[77][2][2] = 1;	this.Code_str[77][3][2] = 0;	this.Code_str[77][4][2] = 0;	this.Code_str[77][5][2] = 0;	this.Code_str[77][6][2] = 0;	this.Code_str[77][7][2] = 1;	this.Code_str[77][8][2] = 0;	this.Code_str[77][9][2] = 1;		
		this.Code_str[77][0][3] = 1;	this.Code_str[77][1][3] = 0;	this.Code_str[77][2][3] = 0;	this.Code_str[77][3][3] = 1;	this.Code_str[77][4][3] = 0;	this.Code_str[77][5][3] = 0;	this.Code_str[77][6][3] = 1;	this.Code_str[77][7][3] = 0;	this.Code_str[77][8][3] = 0;	this.Code_str[77][9][3] = 1;		
		this.Code_str[77][0][4] = 1;	this.Code_str[77][1][4] = 0;	this.Code_str[77][2][4] = 0;	this.Code_str[77][3][4] = 0;	this.Code_str[77][4][4] = 1;	this.Code_str[77][5][4] = 1;	this.Code_str[77][6][4] = 0;	this.Code_str[77][7][4] = 0;	this.Code_str[77][8][4] = 0;	this.Code_str[77][9][4] = 1;		
		this.Code_str[77][0][5] = 1;	this.Code_str[77][1][5] = 0;	this.Code_str[77][2][5] = 0;	this.Code_str[77][3][5] = 0;	this.Code_str[77][4][5] = 0;	this.Code_str[77][5][5] = 0;	this.Code_str[77][6][5] = 0;	this.Code_str[77][7][5] = 0;	this.Code_str[77][8][5] = 0;	this.Code_str[77][9][5] = 1;		
		this.Code_str[77][0][6] = 1;	this.Code_str[77][1][6] = 0;	this.Code_str[77][2][6] = 0;	this.Code_str[77][3][6] = 0;	this.Code_str[77][4][6] = 0;	this.Code_str[77][5][6] = 0;	this.Code_str[77][6][6] = 0;	this.Code_str[77][7][6] = 0;	this.Code_str[77][8][6] = 0;	this.Code_str[77][9][6] = 1;		
		this.Code_str[77][0][7] = 1;	this.Code_str[77][1][7] = 0;	this.Code_str[77][2][7] = 0;	this.Code_str[77][3][7] = 0;	this.Code_str[77][4][7] = 0;	this.Code_str[77][5][7] = 0;	this.Code_str[77][6][7] = 0;	this.Code_str[77][7][7] = 0;	this.Code_str[77][8][7] = 0;	this.Code_str[77][9][7] = 1;		
		this.Code_str[77][0][8] = 1;	this.Code_str[77][1][8] = 0;	this.Code_str[77][2][8] = 0;	this.Code_str[77][3][8] = 0;	this.Code_str[77][4][8] = 0;	this.Code_str[77][5][8] = 0;	this.Code_str[77][6][8] = 0;	this.Code_str[77][7][8] = 0;	this.Code_str[77][8][8] = 0;	this.Code_str[77][9][8] = 1;		
		this.Code_str[77][0][9] = 1;	this.Code_str[77][1][9] = 0;	this.Code_str[77][2][9] = 0;	this.Code_str[77][3][9] = 0;	this.Code_str[77][4][9] = 0;	this.Code_str[77][5][9] = 0;	this.Code_str[77][6][9] = 0;	this.Code_str[77][7][9] = 0;	this.Code_str[77][8][9] = 0;	this.Code_str[77][9][9] = 1;		

		// Символ "N"
		this.Code_str[78][0][0] = 0;	this.Code_str[78][1][0] = 1;	this.Code_str[78][2][0] = 1;	this.Code_str[78][3][0] = 0;	this.Code_str[78][4][0] = 0;	this.Code_str[78][5][0] = 0;	this.Code_str[78][6][0] = 0;	this.Code_str[78][7][0] = 0;	this.Code_str[78][8][0] = 1;	this.Code_str[78][9][0] = 0;		
		this.Code_str[78][0][1] = 0;	this.Code_str[78][1][1] = 1;	this.Code_str[78][2][1] = 0;	this.Code_str[78][3][1] = 1;	this.Code_str[78][4][1] = 0;	this.Code_str[78][5][1] = 0;	this.Code_str[78][6][1] = 0;	this.Code_str[78][7][1] = 0;	this.Code_str[78][8][1] = 1;	this.Code_str[78][9][1] = 0;		
		this.Code_str[78][0][2] = 0;	this.Code_str[78][1][2] = 1;	this.Code_str[78][2][2] = 0;	this.Code_str[78][3][2] = 1;	this.Code_str[78][4][2] = 0;	this.Code_str[78][5][2] = 0;	this.Code_str[78][6][2] = 0;	this.Code_str[78][7][2] = 0;	this.Code_str[78][8][2] = 1;	this.Code_str[78][9][2] = 0;		
		this.Code_str[78][0][3] = 0;	this.Code_str[78][1][3] = 1;	this.Code_str[78][2][3] = 0;	this.Code_str[78][3][3] = 0;	this.Code_str[78][4][3] = 1;	this.Code_str[78][5][3] = 0;	this.Code_str[78][6][3] = 0;	this.Code_str[78][7][3] = 0;	this.Code_str[78][8][3] = 1;	this.Code_str[78][9][3] = 0;		
		this.Code_str[78][0][4] = 0;	this.Code_str[78][1][4] = 1;	this.Code_str[78][2][4] = 0;	this.Code_str[78][3][4] = 0;	this.Code_str[78][4][4] = 1;	this.Code_str[78][5][4] = 0;	this.Code_str[78][6][4] = 0;	this.Code_str[78][7][4] = 0;	this.Code_str[78][8][4] = 1;	this.Code_str[78][9][4] = 0;		
		this.Code_str[78][0][5] = 0;	this.Code_str[78][1][5] = 1;	this.Code_str[78][2][5] = 0;	this.Code_str[78][3][5] = 0;	this.Code_str[78][4][5] = 0;	this.Code_str[78][5][5] = 1;	this.Code_str[78][6][5] = 0;	this.Code_str[78][7][5] = 0;	this.Code_str[78][8][5] = 1;	this.Code_str[78][9][5] = 0;		
		this.Code_str[78][0][6] = 0;	this.Code_str[78][1][6] = 1;	this.Code_str[78][2][6] = 0;	this.Code_str[78][3][6] = 0;	this.Code_str[78][4][6] = 0;	this.Code_str[78][5][6] = 1;	this.Code_str[78][6][6] = 0;	this.Code_str[78][7][6] = 0;	this.Code_str[78][8][6] = 1;	this.Code_str[78][9][6] = 0;		
		this.Code_str[78][0][7] = 0;	this.Code_str[78][1][7] = 1;	this.Code_str[78][2][7] = 0;	this.Code_str[78][3][7] = 0;	this.Code_str[78][4][7] = 0;	this.Code_str[78][5][7] = 0;	this.Code_str[78][6][7] = 1;	this.Code_str[78][7][7] = 0;	this.Code_str[78][8][7] = 1;	this.Code_str[78][9][7] = 0;		
		this.Code_str[78][0][8] = 0;	this.Code_str[78][1][8] = 1;	this.Code_str[78][2][8] = 0;	this.Code_str[78][3][8] = 0;	this.Code_str[78][4][8] = 0;	this.Code_str[78][5][8] = 0;	this.Code_str[78][6][8] = 1;	this.Code_str[78][7][8] = 0;	this.Code_str[78][8][8] = 1;	this.Code_str[78][9][8] = 0;		
		this.Code_str[78][0][9] = 0;	this.Code_str[78][1][9] = 1;	this.Code_str[78][2][9] = 0;	this.Code_str[78][3][9] = 0;	this.Code_str[78][4][9] = 0;	this.Code_str[78][5][9] = 0;	this.Code_str[78][6][9] = 0;	this.Code_str[78][7][9] = 1;	this.Code_str[78][8][9] = 1;	this.Code_str[78][9][9] = 0;		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//	Метод выполняющий чтение BMP-Файла с диска
	//
	//	Входные параметры : BMP_File_Name1 - Имя входного файла содержащего изображение 
	//
	//	Возвращаемое значение : Признак удачного чтения BMP-файла с диска.
	//
	
	int BMP_File_Open(String BMP_File_Name1)
	{
		int Priz_ud;			// Признак удачного выполнения записи BMP-файла на диск
		int poz;				// Указатель позиции при чтении BMP-файла
		int intValueOfByte;		// Вспомогательная переменная - целочислительное значение байта прочитанное из файла
		int	Priz_konec;			// Признак конца файла
		int	X;					// Счетчик столбцов		
		int	Y;					// Счётчик строк
		int	Y_preobraz;			// Преобразованный счётчик строк		
		int intYarkPixel;		// Вспомогательная переменная - яркость пикселя (сумма яркостей красной, голубой и зелёной составляющей).		

		double 	Sum_pryamougolnik;	// Вспомогательная переменная - суммарная яркость пикселей расположенных на прямоугольном участке изображения 
		
		
		Priz_ud = 0;			// Сброс признака удачного считывания BMP-файла с диска

		this.zagolovok 			= new int[54];			// Выделить память для хранения матрицы 54 чисел (заголовок BMP-файла).		
		this.File_Name 			= BMP_File_Name1;		// Записать в поле "File_Name" имя файла содержащего изображение.
		this.File_Input_Stream	= null;					// Обнулить указатель на поток ввода из файла		
		this.File_Input_Buffer	= null;					// Обнулить указатель на буферизированный поток ввода из файла		
		
		System.out.println("\n 	Сообщение из метода 'BMP_File_Open' объекта 'BMP_File' открытие для чтения файла : "+this.File_Name+"  ");

		try 
		{
			// Попытка открыть байтовый буферизированный поток чтения и чтение из входного BMP-файла в специальный буфер.
			this.File_Input_Stream  = new FileInputStream(this.File_Name);				// Создание байтового потока для чтения файла 
			this.File_Input_Buffer 	= new BufferedInputStream(this.File_Input_Stream);	// Создание буферизированного потока ввода из файла и соединение его с байтовым потоком ввода из файла
			
			// Цикл чтения заголовка BMP-файла c помощью байтового потока
			poz = 0;
			while(poz < 54)
			{
				intValueOfByte = this.File_Input_Buffer.read();	// Прочитать очередной байт из буферезированного потока ввода из файла
				if (intValueOfByte == -1) break;				// Достигнут конец файла. Выход из цикла чтения файла.
				this.zagolovok[poz] = intValueOfByte;			// Записать в массив очередной байт заголовка BMP-файла	
				poz++;											// Нарастить счётчик считанных байт из BMP-файла
			}
			
			if (poz == 54)
			{
				// Считан заголовок BMP файла
				for (poz=0; poz<54; poz++) System.out.print("  "+this.zagolovok[poz]);	// Выдать на экран заголовок прочитанного BMP-файла
				System.out.println("  ");												// Перевод строки
				
				if ((this.zagolovok[0] == 'B') && (this.zagolovok[1] == 'M')) 
				{
					// Заголовок начинается с символов "BM"	
					System.out.println("  Начало : BM ");

					// Cчитать смещение области данных относительно начала файла в байтах (позиции 10,11,12,13).	
					this.Sdwig_obl_dann  = this.zagolovok[10] + this.zagolovok[11]*256 + this.zagolovok[12]*256*256 + this.zagolovok[13]*256*256*256; 
					
					// Считать длину информационного заголовка в байтах (позиции 14,15,16,17).
					this.Dlina_zagolovka = this.zagolovok[14] + this.zagolovok[15]*256 + this.zagolovok[16]*256*256 + this.zagolovok[17]*256*256*256;
					
					// Считать размер изображения по горизонтали в пикселях (позиции 18,19,20,21).
					this.Max_X = this.zagolovok[18] + this.zagolovok[19]*256 + this.zagolovok[20]*256*256 + this.zagolovok[21]*256*256*256;
					
					// Считать ширину изображения по вертикали в пикселях (позиции 22,23,24,25).
					this.Max_Y = this.zagolovok[22] + this.zagolovok[23]*256 + this.zagolovok[24]*256*256 + this.zagolovok[25]*256*256*256;	
					
					// Считать количество цветовых плоскостей (позиции 26,27).
					this.Kol_zwet_plosk	= this.zagolovok[26] + this.zagolovok[27]*256;
					
					// Считать количество бит на пиксель (позиции 28,29).
					this.Kol_bit_pixel	= this.zagolovok[28] + this.zagolovok[29]*256;
					
					if (this.Kol_bit_pixel == 24)
					{
						// Данный BMP-файл имеет 24 бита (3 байта)	на пиксель
						
						// Считать тип сжатия данных (позиции 30,31,32,33)
						this.Type_press_data = this.zagolovok[30] + this.zagolovok[31]*256 + this.zagolovok[32]*256*256 + this.zagolovok[33]*256*256*256;

						if (this.Type_press_data == 0)
						{
							// Данный BMP-файл не содержит сжатых данных
							
							// Считать размер графического изображения в байтах (позиции 34,35,36,37)
							this.Size = this.zagolovok[34] + this.zagolovok[35]*256 + this.zagolovok[36]*256*256 + this.zagolovok[37]*256*256*256;
					
							// Считать разрешение по горизонтали (позиции 38,39,40,41)	
							this.Razr_horizont = this.zagolovok[38] + this.zagolovok[39]*256 + this.zagolovok[40]*256*256 + this.zagolovok[41]*256*256*256;
					
							// Считать разрешение по вертикали (позиции 42,43,44,45)
							this.Razr_vertikal = this.zagolovok[42] + this.zagolovok[43]*256 + this.zagolovok[44]*256*256 + this.zagolovok[45]*256*256*256;					

							// Количество добавочных байт в строке (получается как остаток от целочислительного деления BMP_Input_Max_X на 4)							
							this.Kol_dobaw_byte = this.Max_X % 4;
							
							// Считать размер графического изображения в байтах (позиции 34,35,36,37).
							System.out.println(" Сдвиг области данных отн.начала файла в байтах : "+this.Sdwig_obl_dann);
							System.out.println(" Длина информационного заголовка в байтах       : "+this.Dlina_zagolovka);
							System.out.println(" Размер изображения по горизонтали в пикселях   : "+this.Max_X);
							System.out.println(" Размер изображения по вертикали в пикселях     : "+this.Max_Y);
							System.out.println(" Количество цветовых плоскостей                 : "+this.Kol_zwet_plosk);
							System.out.println(" Количество бит на пиксель                      : "+this.Kol_bit_pixel);
							System.out.println(" Тип сжатия данных                              : "+this.Type_press_data);
							System.out.println(" Размер графического изображения в байтах       : "+this.Size);
							System.out.println(" Разрешение по горизонтали                      : "+this.Razr_horizont);
							System.out.println(" Разрешение по вертикали                        : "+this.Razr_vertikal);
							System.out.println(" Количество добавочных байт в строке изображения: "+this.Kol_dobaw_byte);
							
							this.Red	=	new int[this.Max_X][this.Max_Y];		// Выделить память под матрицу хранящую уровни красной составляющей цвета пикселей изображений
							this.Green	=	new int[this.Max_X][this.Max_Y];		// Выделить память под матрицу хранящую уровни зелёной составляющей цвета пикселей изображений
							this.Blue	=	new int[this.Max_X][this.Max_Y];		// Выделить память под матрицу хранящую уровни голубой составляющей цвета пикселей изображений
							this.RGB	= 	new double[this.Max_X][this.Max_Y];		// Выделить память под матрицу хранящую яркости пикселей (среднее арифмитическое красной, голубой, зелёной составляющей)
							
							Priz_konec = 0;							// Сброс признака конца файла	
							
							// Переписывание изображения из BMP-файла в матрицы Red[][], Green[][], Blue[][].
							for (Y=0; Y<this.Max_Y; Y++)
							{
								Y_preobraz = this.Max_Y-Y-1;		// Преобразование номера строки
								
								for (X=0; X<this.Max_X; X++)
								{
									intYarkPixel = 0;								// Обнулить суммарную яркость пикселя 		
									
									intValueOfByte = this.File_Input_Buffer.read();	// Прочитать очередной байт из буферезированного потока ввода из файла
									intYarkPixel += intValueOfByte;					// Прибавить к суммарной яркости пикселя яркость голубой составляющей цвета пикселя
									if (intValueOfByte == -1)
									{
										Priz_konec = 1;								// Установить признака конца файла	
										break;										// Выход из цикла чтения файла.
									}
									this.Blue[X][Y_preobraz] = intValueOfByte;		// Записать очередной считанный байт в матрицу голубой составляющей цвета пикселей 

									intValueOfByte = this.File_Input_Buffer.read();	// Прочитать очередной байт из буферезированного потока ввода из файла
									intYarkPixel += intValueOfByte;					// Прибавить к суммарной яркости пикселя яркость зелёной составляющей цвета пикселя
									if (intValueOfByte == -1)
									{
										Priz_konec = 1;								// Установить признака конца файла	
										break;										// Выход из цикла чтения файла.
									}
									this.Green[X][Y_preobraz] = intValueOfByte;		// Записать очередной считанный байт в матрицу зелёной составляющей цвета пикселей 

									intValueOfByte = this.File_Input_Buffer.read();	// Прочитать очередной байт из буферезированного потока ввода из файла
									intYarkPixel += intValueOfByte;					// Прибавить к суммарной яркости пикселя яркость красной составляющей цвета пикселя
									if (intValueOfByte == -1)
									{
										Priz_konec = 1;								// Установить признака конца файла	
										break;										// Выход из цикла чтения файла.
									}
									this.Red[X][Y_preobraz] = intValueOfByte;			// Записать очередной считанный байт в матрицу красной составляющей цвета пикселей 
									this.RGB[X][Y_preobraz] = (double) intYarkPixel/3;	// Записать в матрицу яркость текущего пикселя (среднее арифмитическое голубой, красной и зелёной составляющей). 
								}
								if (Priz_konec == 1) break;
								
								for (X=0; X<this.Kol_dobaw_byte; X++)
								{
									intValueOfByte = this.File_Input_Buffer.read();	// Прочитать очередной байт из буферезированного потока ввода из файла
									if (intValueOfByte == -1)
									{
										Priz_konec = 1;							// Установить признака конца файла	
										break;									// Выход из цикла чтения файла.
									}
								}
								if (Priz_konec == 1) break;
							}
							
							if (Priz_konec == 0)
							{
								// BMP-файл удалось считать полностью
								System.out.print("  BMP-файл успешно считан. ");	// Выдать на экран сообщение о успешном считывании файла.
								Priz_ud = 1;										// Установить признак удачного считывания BMP-файла с диска.
								Sum_RGB = new double[this.Max_X][this.Max_Y];		// Выделить область памяти для матрицы суммарных яркостей пикселей расположенных левее и выше текущего пикселя
								
								// Заполнение матрицы суммарных яркостей пикселей расположенных левее и выше текущего пикселя
								for (Y=0; Y<this.Max_Y; Y++)
								{
									for (X=0; X<this.Max_X; X++)
									{
										Sum_pryamougolnik = (double) this.RGB[X][Y] - this.get_Sum_RGB(X-1,Y-1) + this.get_Sum_RGB(X,Y-1) + this.get_Sum_RGB(X-1,Y);
										set_Sum_RGB(X, Y, Sum_pryamougolnik);					
									}		
								}
/*
								// Выдать на экран матрицу яркостей
								System.out.println(" ");	
								System.out.println("  Матрица яркостей изображения. ");	
								System.out.println(" ");	
								for (X=0; X<this.BMP_Input_Max_X; X++) System.out.format("         %2d",X);
								System.out.println(" ");		
								System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
								for (Y=0; Y<this.BMP_Input_Max_Y; Y++)
								{
									stroka = String.format("%2d", Y)+" | ";
									for (X=0; X<this.BMP_Input_Max_X; X++)
									{
										stroka += "   "+String.format("%8.2f", this.RGB[X][Y]);
									}
									System.out.println(stroka);
								}

								System.out.println(" * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");	

								// Выдать на экран матрицу суммарных яркостей пикселей
								System.out.println(" ");	
								System.out.println("  Матрица суммарных яркостей изображения. ");	
								System.out.println(" ");	
								for (X=0; X<this.BMP_Input_Max_X; X++) System.out.format("         %2d",X);
								System.out.println(" ");		
								System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
								
								for (Y=0; Y<this.BMP_Input_Max_Y; Y++)
								{
									stroka = String.format("%2d", Y)+" | ";
									for (X=0; X<this.BMP_Input_Max_X; X++)
									{
										stroka += "   "+String.format("%8.2f", this.Sum_RGB[X][Y]);
									}
									System.out.println(stroka);
								}

								System.out.println(" * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");	
*/								
							}
							else
							{
								// BMP-файл закончился раньше чем надо.
								System.out.print("  BMP-файл закончился раньше чем надо. такие файлы не обрабатываются программой ");	// Выдать на экран предупреждающее сообщение.
								System.exit(0);		// Завершение работы программы				
							}
						}
						else
						{
							// Данный BMP-файл содержит сжатые данные
							System.out.print("  Данный BMP-файл содержит сжатые данные, такие файлы не обрабатываются программой ");	// Выдать на экран предупреждающее сообщение.
							System.exit(0);		// Завершение работы программы				
						}
					}
					else
					{
						// Данный BMP-файл имеет не 24 бита (3 байта)на пиксель
						System.out.print("  Данный BMP-файл имеет не 24 бита (3 байта) на пиксель, такие файлы не обрабатываются программой ");	// Выдать на экран предупреждающее сообщение.
						System.exit(0);		// Завершение работы программы				
					}
				}
				else
				{
					// Заголовок начинается не с "BM". Данный файл не является BMP-файлом. 	
					System.out.print("  Данный файл не является BMP-Файлом т.к. заголовок начинается не с 'BM' ");	// Выдать на экран предупреждающее сообщение.
					System.exit(0);		// Завершение работы программы				
				}
			}
			else
			{
				// Не считан заголовок BMP-файла. Этот файл не является BMP-файлом. 
				System.out.print("  Данный файл не является BMP-Файлом т.к. не имеет длину менее 54 байт ");	// Выдать на экран предупреждающее сообщение.
				System.exit(0);		// Завершение работы программы				
			}
		}
		catch(IOException e)
		{
			// Неудача при попытке чтения входного BMP-файла в специальный буфер.
			System.out.print(" !!! Невозможно прочитать файл : "+this.File_Name+"  "+e.toString());	// Выдать на экран предупреждающее сообщение
		}
		finally
		{
			// Окончание. (Сюда попадаем после удачного или неудачного чтения входного BMP-файла в специальный буфер).
			try
			{
				// Попытка закрытия буферизированного потока чтения из входного BMP-файла
				this.File_Input_Buffer.close();	// Закрытие буферизированного потока ввода из файла 
				this.File_Input_Stream.close();	// Закрытие байтового потока ввода из файла
			}
			catch (Exception e1)
			{
				// Неудача при попытке закрытия буферизированного потока чтения из входного BMP-файла
				e1.printStackTrace();	// Выдать сообшение об ошибке и трассировку стека вызова процедур
			}
		}
		
		
		return Priz_ud;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//	Метод выполняющий сохранение BMP-Файла на диск
	//
	//	Возвращаемое значение : Признак удачной записи BMP-файла на диск.
	//

	int BMP_File_Save()
	{
		int Priz_ud;		// Признак удачного выполнения записи BMP-файла на диск
		int i,X,Y;				// Счётчики цикла
		
		Priz_ud = 0;		// Сброс признака удачного записывания BMP-файла на диск
		
		try 
		{
			// Попытка открыть байтовый буферизированный поток записи в BMP-файл протокола.
			this.File_Output_Stream	= new FileOutputStream(this.File_Name);					// Создание байтового потока для записи файла
			this.File_Output_Buffer	= new BufferedOutputStream(this.File_Output_Stream);	// Создание буферизированного потока вывода в файл и соединение его с байтовым потоком вывода из файла

			// Запись заголовка BMP-файла в буферизированный поток вывода
			for (i=0; i<this.Sdwig_obl_dann; i++) this.File_Output_Buffer.write(this.zagolovok[i]);	
			
			// Запись массивов голубой BLUE[][], зелёной GREEN[][], красной RED[][]составляющих цвета пикселей, 
			// в BMP-файл  через буфер.
			for (Y=0; Y<this.Max_Y; Y++)
			{	
				for (X=0; X<this.Max_X; X++)
				{
					this.File_Output_Buffer.write(this.Blue[X][Y]); 
					this.File_Output_Buffer.write(this.Green[X][Y]);
					this.File_Output_Buffer.write(this.Red[X][Y]);					
				}
				for (X=0; X<this.Kol_dobaw_byte; X++) this.File_Output_Buffer.write(0);
			}
			
			Priz_ud = 1;		// Установить признак удачного записывания BMP-файла на диск
		}
		catch (IOException e1)
		{
			// Неудачи при попытке открыть байтовый буферизированный поток записать в BMP-файл протокола.
			System.out.print(" !!! Невозможно записать в файл : "+this.File_Name+"  "+e1.toString());	// Выдать на экран предупреждающее сообщение
		}
		finally
		{
			// Окончание. (Сюда попадаем после удачной или неудачной записи BMP-файла протокола).							
			try
			{
				// Попытка закрытия буферизированного потока записи в BMP-файл протокола.
				this.File_Output_Buffer.flush();		// Сброс буфера на диск
				this.File_Output_Buffer.close();		// Закрытие буферизированного потока вывода в файл 
				this.File_Output_Stream.close();		// Закрытие байтового потока вывода в файл
			}
			catch (Exception e2)
			{
				// Неудача при попытке закрытия буферизированного потока записи в BMP-файл протокола
				e2.printStackTrace();	// Выдать сообшение об ошибке и трассировку стека вызова процедур
			}
		}	

		return Priz_ud;		// Выход из функции записи с возвратом признака удачного записывания BMP-файла на диск. 
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Метод удаление текущего объекта
	//
	
	void dispose()
	{
		this.zagolovok 	= null;		// Освободить память выделенную для хранения матрицы 54 чисел (заголовок BMP-файла).		
		this.Red		= null;		// Освободить память выделенную для хранения уровней красной составляющей цвета пикселей изображения
		this.Green		= null;		// Освободить память выделенную для хранения уровней зелёной составляющей цвета пикселей изображения
		this.Blue		= null;		// Освободить память выделенную для хранения уровней голубой составляющей цвета пикселей изображения
		this.RGB		= null;		// Освободить память выделенную для хранения матрицы хранящую яркости пикселей изображения
		this.Sum_RGB	= null;		// Освободить память выделенную для хранения матрицы хранящую суммарные яркости всех пикселей выше и левее текущей
		this.finalize();
	}
	
	protected void finalize() 
	{
//		   System.out.println("Объект уничтожен");
	}
	
	
	
	
	
}
