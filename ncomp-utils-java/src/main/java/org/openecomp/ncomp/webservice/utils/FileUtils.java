
/*-
 * ============LICENSE_START==========================================
 * OPENECOMP - DCAE
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 */
	
package org.openecomp.ncomp.webservice.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.util.XMLProcessor;

public class FileUtils {
	public static final Logger logger = Logger.getLogger("org.openecomp.ncomp.utils.io");
	static private ResourceSet resourceSet;

	static private ResourceSet createResourceSet() {
		ResourceSet rs = new ResourceSetImpl();
		Resource.Factory binaryResourceFactory = new Resource.Factory() {
			public Resource createResource(URI uri) {
				return new org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl(uri);
			}
		};
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("dat", binaryResourceFactory);
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("dgz", binaryResourceFactory);
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("datzip", binaryResourceFactory);
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmigz", new XMIResourceFactoryImpl());
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmizip", new XMIResourceFactoryImpl());
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("xml", new org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl());
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("xmlgz", new org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl());
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("xmlzip", new org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl());
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		return rs;
	}

	static void init() {
		if (resourceSet == null) {
			resourceSet = createResourceSet();
		}
	}

	static public void ecore2file(EPackage p, EObject ecore, String fileName) throws IOException {
		ecore2file(ecore, fileName);
	}

	static public void ecore2file(EObject ecore, String fileName) throws IOException {
		if (ecore == null) {
			logger.error("Trying to save null object");
			return;
		}
		init();
		Resource resource = resourceSet.createResource(URI.createURI(fileName));
		resource.getContents().add(ecore);
		// error = validate(req,0);
		// if (error != null) throw new Exception("Bad request");
		FileOutputStream fos = new FileOutputStream(FileUtils.safeFileName(fileName));
		Map<String, Object> options = new HashMap<String, Object>();
		// gz is misleading, but supported for backwards compatibility
		if (fileName.endsWith("zip") || fileName.endsWith("gz")) {
			options.put(XMLResource.OPTION_ZIP, Boolean.TRUE);
		}
		options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		try {
			resource.save(fos, options);
		} finally {
			fos.close();
		}
	}

	static public void ecores2file(EList<? extends EObject> ecores, String fileName) throws IOException {
		init();
		Resource resource = resourceSet.createResource(URI.createURI(fileName));
		resource.getContents().addAll(ecores);
		FileOutputStream fos = new FileOutputStream(FileUtils.safeFileName(fileName));
		Map<String, Object> options = new HashMap<String, Object>();
		// gz is misleading, but supported for backwards compatibility
		if (fileName.endsWith("zip") || fileName.endsWith("gz")) {
			options.put(XMLResource.OPTION_ZIP, Boolean.TRUE);
		}
		options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		try {
			resource.save(fos, options);
		} finally {
			fos.close();
		}
	}

	static public EObject file2ecore(EPackage p, String fileName) {
		return file2ecore(fileName, true, true);
	}

	static public EObject file2ecore(String fileName) {
		return file2ecore(fileName, true, true);
	}

	static public EObject file2ecore(EPackage p, String fileName, boolean unload) {
		return file2ecore(fileName, unload, true);
	}

	static public EObject file2ecore(String fileName, boolean unload) {
		return file2ecore(fileName, unload, true);
	}

	static public EObject file2ecore(EPackage p, String fileName, boolean unload, boolean useCommonRS) {
		return file2ecore(fileName, unload, useCommonRS);
	}

	static public EObject file2ecore_old(String fileName, boolean unload, boolean useCommonRS) {
		File file = new File(FileUtils.safeFileName(fileName));
		if (!file.exists()) {
			throw new RuntimeException("File does not exists: " + fileName);
		}
		// URI uri = URI.createFileURI(file.getAbsolutePath());
		URI uri = URI.createURI("file:///" + file.getAbsolutePath().replace("\\", "/"));
		Resource resource;
		logger.debug("Loading " + uri);
		if (useCommonRS) {
			init();
			synchronized (resourceSet) {
				Boolean oldzip = (Boolean) resourceSet.getLoadOptions().get(XMLResource.OPTION_ZIP);
				// gz is misleading, but supported for backwards compatibility
				if (fileName.endsWith("zip") || fileName.endsWith("gz")) {
					resourceSet.getLoadOptions().put(XMLResource.OPTION_ZIP, Boolean.TRUE);
				}
				resource = resourceSet.getResource(uri, true);
				// gz is misleading, but supported for backwards compatibility
				if (fileName.endsWith("zip") || fileName.endsWith("gz")) {
					if (oldzip == null) {
						resourceSet.getLoadOptions().remove(XMLResource.OPTION_ZIP);
					} else {
						resourceSet.getLoadOptions().put(XMLResource.OPTION_ZIP, oldzip);
					}
				}
			}
		} else {
			ResourceSet resourceSet1;
			unload = true; // Need to unload
			resourceSet1 = createResourceSet();
			// gz is misleading, but supported for backwards compatibility
			if (fileName.endsWith("zip") || fileName.endsWith("gz")) {
				resourceSet1.getLoadOptions().put(XMLResource.OPTION_ZIP, Boolean.TRUE);
			}
			resource = resourceSet1.getResource(uri, true);
		}
		EObject e = resource.getContents().get(0);
		if (unload)
			resource.unload();
		return e;
	}

	static public EObject file2ecore(String fileName, boolean unload, boolean useCommonRS) {
		EList<EObject> l = file2ecores(fileName, unload, useCommonRS);
		if (l.size() > 0)
			return l.get(0);
		return null;
	}

	static public EList<EObject> file2ecores(String fileName, boolean unload, boolean useCommonRS) {
		File file = new File(FileUtils.safeFileName(fileName));
		if (!file.exists()) {
			throw new RuntimeException("File does not exists: " + fileName);
		}
		// URI uri = URI.createFileURI(file.getAbsolutePath());
		URI uri = URI.createURI("file:///" + file.getAbsolutePath().replace("\\", "/"));
		Resource resource;
		logger.debug("Loading " + uri);
		EList<EObject> res = new BasicEList<EObject>();
		if (useCommonRS) {
			init();
			synchronized (resourceSet) {
				resource = resourceSet.createResource(uri);
			}
		} else {
			ResourceSet resourceSet1 = createResourceSet();
			// unload = true; // Need to unload
			resource = resourceSet1.createResource(uri);
		}
		Map<String, Object> options = new HashMap<String, Object>();
		// gz is misleading, but supported for backwards compatibility
		if (fileName.endsWith("zip") || fileName.endsWith("gz")) {
			options.put(XMLResource.OPTION_ZIP, Boolean.TRUE);
		}
		options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		try {
			resource.load(options);
		} catch (IOException e1) {
			logger.error("I/O error loading " + safeFileName(fileName) + " : " + e1.getMessage());
			e1.printStackTrace();
			return res;
		} catch (Exception e1) {
			logger.error("Content error loading " + safeFileName(fileName) + " : " + e1.getMessage());
			e1.printStackTrace();
			return res;
		}
		res.addAll(resource.getContents());
		if (unload)
			resource.unload();
		return res;
	}

	static public void clearResourceSet() {
		resourceSet = null;
	}

	protected String validate(Resource resource, int i) {
		for (EObject eObject : resource.getContents()) {
			Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eObject);
			if (diagnostic.getSeverity() != Diagnostic.OK) {
				return printDiagnostic(diagnostic, "");
			}
		}
		return null;
	}

	protected static String printDiagnostic(Diagnostic diagnostic, String indent) {
		System.err.print(indent);
		System.err.println(diagnostic.getMessage());

		StringBuffer buf = new StringBuffer();
		buf.append(indent + diagnostic.getMessage() + "\n");
		for (Diagnostic child : diagnostic.getChildren()) {
			buf.append(printDiagnostic(child, indent + "  ") + "\n");
		}
		return buf.toString();
	}

	static public void ecore2stream(EObject ecore, OutputStream out) {
		init();
		Resource resource = resourceSet.createResource(URI.createURI("http:///foobar"));
		resource.getContents().add(ecore);
		try {
			resource.save(out, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static public BufferedReader filename2reader(String fileName, ErrorMap errors) {
		InputStream s = filename2stream(fileName, errors);
		if (s == null)
			return null;
		return new BufferedReader(new InputStreamReader(s));
	}

	public static InputStream filename2stream(String fileName, ErrorMap errors) {
		InputStream res = null;
		File aFile = new File(FileUtils.safeFileName(fileName));
		if (!aFile.canRead()) {
			// try to see if a file with .gz extention exists.
			aFile = new File(FileUtils.safeFileName(fileName + ".gz"));
			if (aFile.canRead())
				return filename2stream(fileName + ".gz", errors);
			if (errors != null)
				errors.add("Unable to read", fileName);
			return null;
		}
		try {
			logger.debug("Reading " + fileName);
			if (fileName.endsWith(".gz")) {
				try {
					res = new GZIPInputStream(new FileInputStream(FileUtils.safeFileName(fileName)), 524288);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				res = new FileInputStream(aFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	public static ByteBuffer filename2buffer(String fileName, ErrorMap errors) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		InputStream in = filename2stream(fileName, errors);
		if (in == null) {
			return null;
		}
		byte[] buf = new byte[524288];
		int len;
		while (true) {
			try {
				len = in.read(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				errors.add("bad read", fileName);
				break;
			}
			if (len == -1) {
				break;
			}
			b.write(buf, 0, len);
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			errors.add("bad close", fileName);
		}
		return ByteBuffer.wrap(b.toByteArray(), 0, b.size());
	}

	public synchronized static InputStream cmd2stream(String cmd) {
		Runtime runtime = Runtime.getRuntime();
		Process proc;
		try {
			proc = runtime.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROR: " + e);
			return null;
		}
		return proc.getInputStream();
	}

	public synchronized static BufferedReader cmd2reader(String cmd) {
		return new BufferedReader(new InputStreamReader(cmd2stream(cmd)));
	}

	public static OutputStreamWriter filename2writer(String fileName) {
		return filename2writer(fileName, fileName.endsWith(".gz") || fileName.endsWith(".gz.tmp"));
	}

	public static OutputStreamWriter filename2writer(String filename, boolean gzip) {
		try {
			File f = new File(FileUtils.safeFileName(filename));
			if (f.exists()) f.delete();
			String p = f.getParent();
			if (p != null) {
				File d = new File(p);
				d.mkdirs();
			}
			if (gzip) {
				OutputStream s = new GZIPOutputStream(new FileOutputStream(FileUtils.safeFileName(filename)), 524288);
				return new OutputStreamWriter(s);
			} else
				return new FileWriter(FileUtils.safeFileName(filename));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void find(String dirName, String regex, List<String> res) {
		File dir = new File(FileUtils.safeFileName(dirName));
		String[] children = dir.list();
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				String ff = dirName + "/" + children[i];
				File f = new File(FileUtils.safeFileName(ff));
				if (f.isDirectory()) {
					find(ff, regex, res);
				} else {
					if (children[i].matches(regex)) {
						res.add(ff);
					}
				}
			}
		}
	}

	public static List<String> find(String dir, String regexp) {
		List<String> res = new ArrayList<String>();
		find(dir, regexp, res);
		return res;
	}

	public static void ecore2xmlfile(XMLProcessor x, EObject doc, String filename) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(FileUtils.safeFileName(filename));

			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put("xml", new org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl());
			URI uri = URI.createURI(filename);
			Resource resource = resourceSet.createResource(uri);
			resource.getContents().add(doc);
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(XMLResource.OPTION_EXTENDED_META_DATA, x.getExtendedMetaData());
			x.save(fos, resource, options);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void mkdirForFile(String filename) {
		File f = new File(FileUtils.safeFileName(filename));
		f.getParentFile().mkdirs();
	}

	public static boolean uptodate(String filename, String interval) {
		File file = new File(FileUtils.safeFileName(filename));
		if (!file.exists())
			return false;
		Date now = new Date();
		if (file.lastModified() + DateUtils.stringToDuration(interval) > now.getTime())
			return true;
		return false;
	}

	public static void touch(String filename) throws IOException {
		File file = new File(FileUtils.safeFileName(filename));
		file.createNewFile();
		Date now = new Date();
		file.setLastModified(now.getTime());
	}

	public static void copyFile(String sourceFile, String destFile) throws IOException {
		File from = new File(FileUtils.safeFileName(sourceFile));
		File to = new File(FileUtils.safeFileName(destFile));
		copyFile(from, to);
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(FileUtils.safeFile(sourceFile)).getChannel();
			destination = new FileOutputStream(FileUtils.safeFile(destFile)).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static void copyDirectory(File sourceDir, File destDir) throws IOException {
		logger.debug("copy: " + sourceDir.getAbsolutePath() + " " + destDir.getAbsolutePath());
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		for (File f : sourceDir.listFiles()) {
			File dest = createSafeFile(destDir, f.getName());
			if (f.isDirectory()) {
				copyDirectory(f, dest);
				continue;
			}
			if (f.isFile()) {
				copyFile(f, dest);
			}
		}
	}

	public static void deleteDirectory(File dir) {
		if (!dir.exists()) {
			throw new RuntimeException("Directory does not exists: " + dir.getAbsolutePath());
		}
		if (!dir.isDirectory()) {
			throw new RuntimeException("Is not a directory: " + dir.getAbsolutePath());
		}

		logger.debug("deleting: " + dir.getAbsolutePath());
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				deleteDirectory(f);
				continue;
			}
			if (f.isFile()) {
				f.delete();
			}
		}
		dir.delete();
	}

	public static Object file2object(String filename) {
		try {
			InputStream in = filename2stream(filename, null);
			if (in == null)
				return null;
			ObjectInputStream r = new ObjectInputStream(in);
			Object o;
			try {
				o = r.readObject();
			} finally {
				in.close();
			}
			return o;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void object2file(Object o, String filename) {
		// TODO Auto-generated method stub
		try {
			String f = filename + ".tmp";
			File f1 = new File(safeFileName(f));
			File f2 = new File(safeFileName(filename));
			if (!f1.getParentFile().exists()) f1.getParentFile().mkdirs();
			ObjectOutputStream w = new ObjectOutputStream(new FileOutputStream(safeFileName(f)));
			try {
				w.writeObject(o);
				w.flush();
			} finally {
				w.close();
			}
			f1.renameTo(f2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void copyStream(InputStream in, OutputStream out) throws IOException {
		copyStream(in, out, 524288);
	}

	public static void copyStream(InputStream in, OutputStream out, int bufferSize) throws IOException {
		byte[] buf = new byte[bufferSize];
		int len = 0;
		while ((len = in.read(buf)) >= 0) {
			out.write(buf, 0, len);
		}
	}

	public static void deleteEmptyDirectories(HashSet<File> dirs) {
		HashSet<File> dirs1 = new HashSet<File>();
		for (File d : dirs) {
			if (!d.exists() || !d.isDirectory()) continue;
			if (d.listFiles().length > 0) continue;
			if (!d.delete()) 
				logger.warn("unable to delete directory: " + d.getAbsolutePath());
			dirs1.add(d.getParentFile());
		}
		if (dirs1.size() > 0)
			deleteEmptyDirectories(dirs1);
	}

	public static int cmd(String cmd, StringBuffer outBuf, StringBuffer errBuf) {

		Runtime runtime = Runtime.getRuntime();
		Process proc;
		try {
			proc = runtime.exec(cmd);
			ByteArrayOutputStream o = new ByteArrayOutputStream();
			ByteArrayOutputStream e = new ByteArrayOutputStream();
			copyStream(proc.getInputStream(), o);
			copyStream(proc.getErrorStream(), e);
			int i = proc.waitFor();
			outBuf.append(o.toString());
			errBuf.append(e.toString());
			return i;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR: " + e);
			errBuf.append("ERROR: " + e + "\n");
			return -1;
		}
	}

	public static File createSafeFile(File dir, String fname) {
		String fname2 = dir.getAbsolutePath() + "/" + fname;
		return new File(safeFileName(fname2));
	}

	public static String safeFileName(String file) {
		// creating file with safer creation. 
		if (file.contains("../")) 
			throw new RuntimeException("File name contain ..: " + file);
		if (file.contains("\n")) 
			throw new RuntimeException("File name contain newline: " + file);
		return file;
	}
	
	private static File safeFile(File file) {
		// creating file with safer creation. 
		if (file.getAbsolutePath().contains("..")) 
			throw new RuntimeException("File name contain ..: " + file.getAbsolutePath());
		return file;
	}

	public static Thread copyStreamThread(final InputStream inputStream, final OutputStream outputStream) {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					copyStream(inputStream, outputStream);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
		return t;
	}

}
