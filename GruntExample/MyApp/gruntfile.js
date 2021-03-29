module.exports = function(grunt){
    grunt.initConfig({
        //Package containing our Node modules
        pkg: grunt.file.readJSON('package.json'),
        
        //Tasks
        //Copy Task - Copies contents of dir1 to dir2
        copy: {
            //Targets/Context
            t1: {
                src: 'dir1/**',
                dest: 'dist/'
            }
        }, 
        //Concat Task - Takes js or css files and merges into one file.
        concat: {
            js: {
                src:'js/*',
                dest: 'build/js/scripts.js'
            },
            css: {
                src: 'css/*',
                dest: 'build/css/styles.css'
            }
        },
        uglify: {
            dist:{

            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.registerTask('default', 'copy:t1');
    
};