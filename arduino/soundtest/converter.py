import sys

print sys.argv

if len(sys.argv) < 2:
    print "No infile specified"
    exit

songname = sys.argv[1].split('/')[-1].split('.')[0]

with open(sys.argv[1],'rb') as infile:
    with open(sys.argv[1]+".h",'w') as out:
        b = infile.read(1)
        out.write("const unsigned char "+songname+"[] {")
        if b != "":
            out.write(str(ord(b)))
            b = infile.read(1)
        while b != "":
            out.write(","+str(ord(b)))
            b = infile.read(1)
        out.write("};")
        
