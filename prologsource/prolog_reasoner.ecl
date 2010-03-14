% Copyright (C) 2008 Stephan Schiffel <stephan.schiffel@gmx.de>

:- module(prolog_reasoner).

:- export
	parse/2,
	translate_to_sexpr/2,
	goal/3,
	legal_moves/3,
	state_update/3,
	terminal/1,
	setgame/1,
	init/1,
	roles/1.

:- use_module(gdl_parser).
:- use_module(game_description_interface).

:- lib(lists).

parse(String, Term) :-
	parse_gdl_term_string(String, Term).

translate_to_sexpr(Term, String) :-
	translate_to_sexpr_string(Term, String).

goal(Role, Value, State) :-
	d_goal(Role, Value, nil-State).

legal_moves(Role, Moves, State) :-
	fast_setof(does(Role, M), d_legal(Role, M, nil-State), Moves).

state_update(State, Moves, NextState) :-
	fast_setof(F, d_next(F, Moves-State), NextState).

terminal(State) :-
	d_terminal(nil-State).
	
setgame(GDLDescriptionString) :-
	parse_gdl_description_string(GDLDescriptionString, Rules),
	sort_clauses(Rules, Rules1),
	compile_new_rules(Rules1).

% sort the clauses in a List alphabetically according to the name of the head
:- mode sort_clauses(+,-).
sort_clauses(List1, List2) :-
	(foreach(Clause,List1), foreach(key(Name,Arity)-Clause,KeyList1) do
		(Clause= (:- _) ->
			Name=0, Arity=0 % 0 is a number and comes before every atom according to @</2
		;
			(Clause=(Head:-_) -> true ; Clause=Head),
			functor(Head,Name,Arity)
		)
	),
	keysort(KeyList1,KeyList2),
	(foreach(key(_,_)-Clause,KeyList2), foreach(Clause,List2) do true),!.


init(State) :-
	fast_setof(F, d_init(F, nil-nil), State).
	
roles(Roles) :-
	findall(R, d_role(R, nil-nil), Roles).
	
fast_setof(X,Expr,Xs) :-
	findall(X,Expr,Xs1),
	sort(0,<,Xs1,Xs).
	

