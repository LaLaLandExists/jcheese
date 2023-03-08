import os
import sys

def process(filename):
	with open(filename, 'r') as file:
		content = file.read()
		print(f'Got {content}')
		string_buffer = []
		
		for i in range(0, len(content)):
			char = content[i]
			if char == '\t':
				string_buffer.append('  ')
			else:
				string_buffer.append(char)
		
		output = ''.join(string_buffer)
		print(output)
		return output

def get_java_files(dir):
	files = []
	
	for file in os.scandir(dir):
		if file.is_dir():
			files.extend(get_java_files(file.path))
		elif file.is_file():
			if os.path.splitext(file.name)[1].lower() == '.java':
				files.append(file.path)
	return files

def main(args):
	try:
		dir_start = args[1]
		files = get_java_files(dir_start)
		for filename in files:
			new_content = process(filename)
			with open(filename, 'w') as file:
				file.seek(0)
				file.truncate()
				file.write(new_content)
	except IndexError:
		print("Expect one argument")

if __name__ == '__main__':
	main(sys.argv)