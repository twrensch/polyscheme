;; Initialization file for scheme
;; All but the core functions are here for now. They will be moved to
;; primitive functions as needed for speed or functionality.
;;
;; Created: twrensch 17 June 2012
;; Updated: twrensch 23 June 2012
;; Updated: twrensch 05 May  2014

;; Shift type-specific operations to the appropriate types

(set! gcopy (fn (type sym) (acc type sym (acc *G* sym))))
(set! gmove (fn (type sym) (gcopy type sym) (acc *G* sym #n)))

(do (fn (sym) (gmove PAIR sym)) '(car cdr set-car! set-cdr! cadr cddr))
(do (fn (sym) (gcopy STRING sym)) '(+ < >))
(do (fn (sym) (gmove NUMBER sym)) '(+ * ^ / - % < > floor))
(gmove PROCEDURE 'makemacro)
(acc ARRAY 'get acc)
(acc ARRAY 'set acc)

(set! gtype (fn (t s) (acc t s true) (acc *G* s false)))
(gtype PAIR 'pair?)
(gtype NUMBER 'number?)
(gtype ARRAY 'array?)
(gtype SYMBOL 'symbol?)
(gtype NULL 'null?)
(gtype TYPE 'type?)
(gtype PROCEDURE 'procedure?)
(gtype STRING 'string?)
(gtype BOOLEAN 'boolean?)
	

;; def and defm
(set! def (makemacro (fn (params . body)
	(if (pair? params)
			(list 'set! (car params) (cons 'fn (cons (cdr params) body)))
			(list 'set! params (car body)) ))))
			
(set! defm (makemacro (fn (type params . body)
	(list 'acc type (list 'quote (car params))
		(cons 'fn (cons (cdr params) body))) ))))

;; Basics

(def empty? true)
(defm PAIR (empty? x) (= x ()))

(def (map1 f lst)
	(def (helper lst sofar)
		(if (empty? lst)
				sofar
				(helper (cdr lst) (cons (f (car lst)) sofar)) ))
	(reverse! (helper lst ())) )
(def map map1)

(def (select1 pred lst)
	(def (helper lst sofar)
		(if (empty? lst)
				sofar
				(helper (cdr lst) (if (pred (car lst)) (cons (car lst) sofar) sofar)) ))
	(reverse! (helper lst ())) )
(def select select1)

;; Functions to support REPL and end-user programming

;; load given path name into global environment
(def (load filename)
	(def f (java.io.File filename)) 
	(def r (.load *PSCHEME* f))
	(.close f)
	r)

;; Basic eval, global only
(def (eval str) (.load *PSCHEME* (java.io.StringReader str)))

;; Open a workspace window, return the workspace object
(def (workspace path)
	(def w (com.tomwrensch.pscheme.tools.Workspace *PSCHEME*)) 
	(.open w (if (null? path) #n (java.io.File path)))
	w)

;; Exit function
(def (exit code) (.exit java.lang.System (if (number? code) code 0)))

;; Create an array with the given values
(def (array . items)
	(def vect (makearray (len items)))
	(def (helper i lst)
		(if (empty? lst) 
				vect 
				(begin (set vect i (car lst)) (helper (+ i 1) (cdr lst))) ))
	(helper 0 items) )

;; Type support
(def (tostring obj) (.stringify com.tomwrensch.pscheme.U obj))
(def (maketype name) (com.tomwrensch.pscheme.SchemeType (tostring obj)))
;; needs java support - typed interface and typedvalue object maybe records?


;; Timing Test (initial smallest of 5 is 1251)
;;   with fastminus 1162
(def (timetest)
	(def (one x) x)
	(def (lp n) (if (> 1 n) 0 (begin (one n) (lp (- n 1)))))
	(def t (.currentTimeMillis java.lang.System))
	(lp 1000000)
	(- (.currentTimeMillis java.lang.System) t))
	
;; Most people are used to get and set rather than acc
(def (get table key) (acc table key))
(def (set table key value) (acc table key value))
