/**
 * Decision Tree Classification With Uncertain Data (UDT)
 * Copyright (C) 2009, The Database Group,
 * Department of Computer Science, The University of Hong Kong
 *
 * This file is part of UDT.
 *
 * UDT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UDT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.decisiontree.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.decisiontree.build.TreeNode;
import com.decisiontree.exceptions.DecisionTreeFileException;
import com.thoughtworks.xstream.XStream;

/**
 *
 * DecisionTreeStorage - saving the tree and reading the tree from files.
 *
 * @author Smith Tsang
 * @since 0.9
 *
 */
public class DecisionTreeStorage {

	private XStream xstream = new XStream();
	private static Logger log = Logger.getLogger(DecisionTreeStorage.class);


	private void creatFileAndDirectory(String fileName) throws IOException{
		File file = new File(fileName);
		if(!file.exists()){
			if(file.getParentFile()!= null)
				file.getParentFile().mkdirs();
			file.createNewFile();
		}
	}

	public void saveTreeToFile(String fileName, TreeNode tree) throws DecisionTreeFileException{

		try{
			creatFileAndDirectory(fileName);
			saveTreeToWriter(new FileOutputStream(fileName), tree);
		}catch(IOException e){
			log.error(e.getMessage(),e);
			throw new DecisionTreeFileException(e);
		}
	}

	private void saveTreeToWriter(OutputStream outputStream, TreeNode tree) throws DecisionTreeFileException{

		ObjectOutputStream out = null;
		try{
			out = xstream.createObjectOutputStream(outputStream);
			out.writeObject(tree);
		}catch(IOException e){
			throw new DecisionTreeFileException(e);
		}
		finally{
			try{
				if (out!= null)out.close();
			}catch(IOException e){
				throw new DecisionTreeFileException(e);
			}
		}
	}

	public TreeNode readTreeFromFile(String fileName) throws DecisionTreeFileException{

		try {
			return readTreeFromReader(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(),e);
			throw new DecisionTreeFileException(e);
		}
	}

	private TreeNode readTreeFromReader(InputStream inputStream) throws DecisionTreeFileException{

		ObjectInputStream in = null ;
		try{
			in = xstream.createObjectInputStream(inputStream);
			return (TreeNode)in.readObject();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			throw new DecisionTreeFileException(e);
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage(),e);
			throw new DecisionTreeFileException(e);
		}finally{
			try{
				if(in!=null) in.close();
			}catch(IOException e){
				log.error(e.getMessage(),e);
				throw new DecisionTreeFileException(e);
			}
		}

	}

}
