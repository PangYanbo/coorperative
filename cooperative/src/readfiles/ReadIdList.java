package readfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class ReadIdList {
	public static void read_id_list(File in, ArrayList<String>id_List) throws IOException, ParseException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		id_List = new ArrayList<String>();
		while((line=br.readLine())!=null){
			String id = line;
			id_List.add(id);
		}
		br.close();
	}
}
