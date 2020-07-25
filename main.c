#include<stdio.h>
#include <sys/types.h>
#include <string.h>
#include <dirent.h>
#include <unistd.h>
#include <stdlib.h>

char isXposed(char *filename) {
    return strstr(filename, "xposed") != NULL;
}

long filelength(FILE *fp) {
    long num;
    fseek(fp, 0, SEEK_END);
    num = ftell(fp);
    fseek(fp, 0, SEEK_SET);
    return num;
}

char isXposedMaps(char *pid) {
    FILE *maps;
    char path[80];
    char *content;
    strcpy(path, "/proc/");
    strcat(path, pid);
    strcat(path, "/maps");
    if ((maps = fopen(path, "r")) == NULL) {
        return 0;
    } else {
        int len = filelength(maps);
        content = (char *) malloc(len);
        fread(content, len, 1, maps);
        content[len - 1] = '\0';
        return strstr(content, "XposedBridge") != NULL;
    }
}

int main(int argc, char **argv) {
    /*DIR * dir;
    struct dirent * ptr;
    dir = opendir("/system/lib");
    printf("fuck the wechat and tencent.");
    while(ptr = readdir(dir)){
        if(isXposed(ptr->d_name)){
            closedir(dir);
            return 0;
        }
    }
    closedir(dir);
*/
    if (argc == 2 && isXposedMaps(argv[1]))
        return 0;
    return 1;
}
