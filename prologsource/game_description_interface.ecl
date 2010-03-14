% Copyright (C) 2008 Stephan Schiffel <stephan.schiffel@gmx.de>

:- module(game_description_interface, [
	% interface to gdl games

	% game stuff
	d_goal/3,
	d_legal/3,
	d_terminal/1,
	d_role/2,
	d_next/2,
	d_init/2,
	d_does/3,
	d_true/2,

	% loading the rules of the game, and/or overriding the rules with better ones
	compile_new_rules_from_file/1,
	compile_new_rules/1
	], eclipse_language).
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:- mode compile_new_rules_from_file(++).
compile_new_rules_from_file(Filename) :-
	compile(Filename).

:- mode compile_new_rules(+).
compile_new_rules(Clauses) :-
	expand_goal(Clauses, Clauses1),
	% make sure we don't get interrupted during compiling (we could end up in some inconsistent state if we have only half of the clauses)
	(events_defer ->
		(compile_term(Clauses1) -> events_nodefer ; events_nodefer, fail)
	;
		compile_term(Clauses1)
	).

:- mode d_does(?, ?, ++).
d_does(R,A,Moves-_Z) :-
	member(does(R,A),Moves).

:- mode d_true(?, +).
d_true(F,_-Z) :-
	member(F,Z).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% the following will be overwritten by created stuff 

:- mode d_role(?, ++).
d_role(_, _) :- fail.

:- mode d_init(?, ++).
d_init(_, _) :- fail.

:- mode d_next(?, ++).
d_next(_, _) :- fail.

:- mode d_legal(?, ?, ++).
d_legal(_, _, _) :- fail.

:- mode d_goal(?, ?, ++).
d_goal(_,_, _) :- fail.

:- mode d_terminal(++).
d_terminal(_) :- fail.

