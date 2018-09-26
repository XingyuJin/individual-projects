(define (caar x) (car (car x)))
(define (cadr x) (car (cdr x)))
(define (cdar x) (cdr (car x)))
(define (cddr x) (cdr (cdr x)))

; Some utility functions that you may find useful to implement.

(define (cons-all first rests)
  (if (null? rests)
    (list (cons first nil))
    (map (lambda (x) (append (cons first nil) x)) rests)
  ))

(define (zip pairs)
  (define (get-first pairs)
    (if (null? pairs)
      nil
      (cons (car (car pairs)) (get-first (cdr pairs)))
    ))

  (define (get-second pairs)
    (if (null? pairs)
      nil
      (cons (car (cdr (car pairs))) (get-second (cdr pairs)))
    ))

  (if (null? pairs)
    '(() ())
    (append (list (get-first pairs)) (list (get-second pairs)))
  ))

;; Problem 17
;; Returns a list of two-element lists
(define (enumerate s)
  (define (index-num index s)
    (if (null? s)
      nil
      (cons (cons index (cons (car s) nil)) (index-num (+ index 1) (cdr s)))
    ))
  (index-num 0 s)
)


;; Problem 18
;; List all ways to make change for TOTAL with DENOMS
(define (list-change total denoms)
  (cond ((null? denoms) nil)
        ((< total (car denoms)) (list-change total (cdr denoms)))
        ((<= total 0) nil)
        (else (append
                (cons-all (car denoms) (list-change (- total (car denoms)) denoms))
                (list-change total (cdr denoms))
              )
        )
  )
)


;; Problem 19
;; Returns a function that checks if an expression is the special form FORM
(define (check-special form)
  (lambda (expr) (equal? form (car expr))))

(define lambda? (check-special 'lambda))
(define define? (check-special 'define))
(define quoted? (check-special 'quote))
(define let?    (check-special 'let))

;; Converts all let special forms in EXPR into equivalent forms using lambda
(define (let-to-lambda expr)
  (cond ((atom? expr)
         expr
         )
        ((quoted? expr)
         (cons 'quote (cdr expr))
         )
        ((or (lambda? expr)
             (define? expr))
         (let ((form   (car expr))
               (params (cadr expr))
               (body   (cddr expr)))
           (cons form (cons params (map let-to-lambda body)))
           ))
        ((let? expr)
         (let ((values (cadr expr))
               (body   (cddr expr)))
           (cons (cons 'lambda (cons (car (zip values)) (map let-to-lambda body))) (map let-to-lambda (cadr (zip values))))
           ))
        (else
           (map let-to-lambda expr)
         )

         )))


;; (cons (let-to-lambda (car expr)) (let-to-lambda (cdr expr)))

  ;;       scm> (let-to-lambda '(lambda (let a b) (+ let a b)))
      ;;   (lambda (let a b) (+ let a b))
      ;;   scm> (let-to-lambda '(lambda (x) a (let ((a x)) a)))
      ;;   (lambda (x) a ((lambda (a) a) x))
