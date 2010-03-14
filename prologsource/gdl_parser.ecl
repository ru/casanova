% Copyright (C) 2008 Stephan Schiffel <stephan.schiffel@gmx.de>

:- module(gdl_parser).

:- export(parse_gdl_description_string/2).
:- export(parse_gdl_message_string/2).
:- export(parse_gdl_term_string/2).
:- export(translate_to_sexpr_string/2).

:- mode parse_gdl_description_string(+,-).
parse_gdl_description_string(String,Axioms) :-
	parse_string(String, gdl_knowledge_base(Axioms)).

:- mode parse_gdl_message_string(+,-).
parse_gdl_message_string(String,Message) :-
	parse_string(String, gdl_message(Message)).

:- mode parse_gdl_term_string(+,-).
parse_gdl_term_string(String,Term) :-
	parse_string(String, gdl_term(Term)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% :- lib(var_name).
:- lib(hash).
:- local reference(variables).

:- mode parse_string(+,?).
parse_string(String, GDL_Part) :-
	string_list(String, List),
	hash_create(Variables), setval(variables,Variables),
	phrase(GDL_Part, List, Remainder),
	(Remainder=[] ->
		true
	;
		string_list(RemainderString, Remainder),
		printf("gdl parse error before: \"%w\"\n", [RemainderString]),
		fail
	).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
rename_predicate(Pred,State,Pred2) :-
	Pred=..[PName|Args],
	append(Args, [State], Args2),
	concat_atoms(d_,PName,PName2),
	Pred2=..[PName2|Args2].

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%gdl_upper(C) --> [C], {uppercasechar(C), !}.
uppercasechar(C) :- C>=65, C=<90. % C>="A" , C=<"Z"

%gdl_lower(C) --> [C], {lowercasechar(C), !}.
lowercasechar(C) :- C>=97, C=<122. % C>="a" , C=<"z"

gdl_digit(C) --> [C], {digit(C), !}.
digit(C) :- C>=48, C=<57. % C>="0" , C=<"9"

%gdl_alpha(C) --> [C], {alpha(C), !}.
alpha(33).  % "!"
alpha(36).  % "$"
alpha(37).  % "%"
alpha(38).  % "&"
alpha(42).  % "*"
alpha(43).  % "+"
alpha(45).  % "-"
alpha(46).  % "."
alpha(47).  % "/"
alpha(60).  % "<"
alpha(61).  % "="
alpha(62).  % ">"
alpha(63).  % "?"
alpha(64).  % "@"
alpha(95).  % "_"
alpha(126). % "~"

% gdl_special --> ["\""]; ["#"]; ["'"]; ["("]; [")"]; [","]; ["\\"]; ["^"]; ["`"].

gdl_left_parens			--> [40]. % "("
gdl_right_parens		--> [41]. % ")"
% gdl_backslash(92)		--> [92]. % "\"
gdl_hashmark			--> [35]. % "#"
gdl_newline				--> [10]. % "\n"
% gdl_quotationmark(34)	--> [34]. % """
gdl_questionmark		--> [63]. % "?"
gdl_pipe				--> [124]. % "|"

gdl_white --> [59] /* ";" */, !, all_to_end_of_line. % single line comment
gdl_white --> gdl_hashmark, gdl_pipe, !, all_to_end_of_comment. % multi line comment
gdl_white --> [C], {whitespace(C), !}. % normal whitespace

whitespace(32). % " "
whitespace(9).  % "\t"
whitespace(13). % "\r"
whitespace(10). % "\n"
whitespace(12). % "\f"

end_of_file([],[]).

all_to_end_of_line --> gdl_newline, !. % "\n"
all_to_end_of_line --> end_of_file, !. % EOF
all_to_end_of_line --> [_], all_to_end_of_line.

all_to_end_of_comment --> gdl_pipe, gdl_hashmark, !.
all_to_end_of_comment --> [_], all_to_end_of_comment.


gdl_white_plus --> gdl_white, !, gdl_white_star.
gdl_white_star --> gdl_white, !, gdl_white_star.
gdl_white_star --> [].

%gdl_normal(C) --> gdl_upper(C),! ; gdl_lower(C),! ; gdl_digit(C),! ; gdl_alpha(C),!.
gdl_normal(C) --> [C], {(uppercasechar(C); lowercasechar(C); digit(C); alpha(C)),!}.

% % quotedchar ::= \character
% gdl_quotedchar([C1,C2]) --> gdl_backslash(C1), [C2].

% word ::= normal | word normal | word\character
gdl_word(String) --> gdl_normal(C1), gdl_word2(CList), {string_list(String, [C1|CList])}.
gdl_word2(List) --> gdl_word2_nonempty(List), !.
gdl_word2([]) --> [].
gdl_word2_nonempty([C|List]) --> gdl_normal(C), !, gdl_word2(List).
% gdl_word2_nonempty(List2) --> gdl_quotedchar(Cs), !, gdl_word2(List), {append(Cs,List,List2)}.

% % charref ::= #\character
% gdl_charref(String) --> gdl_hashmark(C), gdl_quotedchar(Cs), {string_list(String,[C|Cs])}.

% % string ::= "quotable"
% gdl_string(String) --> gdl_quotationmark(C), gdl_quotable(CList), gdl_quotationmark(C), {append([C|CList],[C],List), string_list(String,List)}.
% 
% % quotable ::= empty | quotable strchar | quotable\character
% gdl_quotable(List2) --> gdl_quotedchar(Cs), !, gdl_quotable(List), {append(Cs,List,List2)}.
% gdl_quotable([C|List]) --> gdl_strchar(C), !, gdl_quotable(List).
% gdl_quotable([]) --> [].
% 
% % strchar ::= character - {",\}
% gdl_strchar(C) --> [C], {C\=34, C\=92}.

% variable ::= indvar | seqvar
% gdl_variable(Var) --> gdl_indvar(Var).

% indvar ::= ?word
gdl_indvar(Var) --> gdl_questionmark, gdl_word(VarName),
			{
			getval(variables,Variables),
			(hash_get(Variables, VarName, Var) ->
				true
			;
				hash_set(Variables, VarName, Var)
%				,first_char_to_upper(VarName,UppercaseVarName), set_var_name(Var,UppercaseVarName)
			)
			}.
% seqvar ::= @word
% there are no sequence variables in gdl

first_char_to_upper(StringLower,StringUpper) :-
	string_list(StringLower,[CLower|List]),
	char_to_upper(CLower,CUpper),
	string_list(StringUpper,[CUpper|List]).
char_to_upper(CLower,CUpper) :- lowercasechar(CLower) -> CUpper is CLower-32 ; CUpper=CLower.

% operator ::= termop | sentop | defop
gdl_operator(String) --> gdl_sentop(String). % others are not valid gdl

% termop ::= value | listof | quote | if
% defop ::= defobject | defunction | defrelation | deflogical | := | :-> | :<= | :=>
% sentop ::= holds | = | /= | not | and | or | => | <= | <=> | forall | exists
gdl_sentop(String) --> (gdl_not(String); gdl_and(String); gdl_or(String); gdl_inv_impl(String)), !. % others are not valid gdl
gdl_not("not") --> [110,111,116], gdl_word2(W), {W=[]}. % ="not"
gdl_and("and") --> [97,110,100], gdl_word2(W), {W=[]}. % ="and"
gdl_or("or") --> [111,114], gdl_word2(W), {W=[]}. % ="or"
gdl_inv_impl("<=") --> [60,61], gdl_word2(W), {W=[]}. % ="<="

% constant ::= word - variable - operator
gdl_constant(Atom) --> gdl_constant2(CList), {string_list(String,CList), atom_string(Atom,String)}.
% the next clause can be safely ignored if we don't want to check the description for correctness
% gdl_constant2([String|List]) --> gdl_operator(String), !, gdl_word2_nonempty(List).
gdl_constant2([C|List]) --> gdl_normal(C), {C\=63}, gdl_word2(List). % C\="?"

% term ::= indvar | constant | charref | string | block | funterm | listterm | quoterm | logterm
gdl_term(Term) --> gdl_indvar(Term) ; gdl_funterm(Term) ; gdl_constant_term(Term). % others are not valid gdl

% gdl_term_star_right_parens ::= term* )
gdl_term_star_right_parens(Terms) --> gdl_white_star, gdl_term_star_right_parens2(Terms).
gdl_term_star_right_parens2([]) --> gdl_right_parens, !.
gdl_term_star_right_parens2([Term|Terms]) --> gdl_term(Term), gdl_term_star_right_parens(Terms).

gdl_term_list([]) --> [110,105,108]. % ="nil"
gdl_term_list(Terms) --> gdl_left_parens, gdl_term_star_right_parens(Terms).

% funterm ::= (constant term* [seqvar])
% funterm ::= (value term term* [seqvar])
% only the first form and without seqvar is valid gdl
gdl_funterm(Function) --> gdl_left_parens, gdl_white_star, gdl_constant(Atom), gdl_term_star_right_parens(Terms), {Function=..[Atom|Terms]}.

% listterm ::= (listof term* [seqvar])
% quoterm ::= (quote listexpr) | 'listexpr
% listexpr ::= atom | (listexpr*)
% atom ::= word | charref | string | block
% logterm ::= (if logpair+ [term])
% logpair ::= sentence term
% logterm ::= (cond logitem*)
% logitem ::= (sentence term)

gdl_constant_term(Term) --> gdl_integer(Term), ! ; gdl_constant(Term).

% sentence ::= constant | equation | inequality | relsent | logsent | quantsent
%gdl_sentence(Sentence) --> gdl_logsent(Sentence) ; gdl_relsent(Sentence) ; gdl_constant(Sentence). % others are not valid gdl
gdl_sentence(Sentence, State) --> gdl_left_parens, !, gdl_white_star, gdl_complex_sentence_right_parens(Sentence, State).
gdl_sentence(Atom, State) --> gdl_relation_constant(Atom, State).
gdl_complex_sentence_right_parens(Sentence, State) -->	gdl_logsent2_right_parens(Sentence, State), !.
gdl_complex_sentence_right_parens(Sentence, State) -->	gdl_relsent2_right_parens(Sentence, State), !.

gdl_sentence_star_right_parens(Sentences, State) --> gdl_white_star, gdl_sentence_star_right_parens2(Sentences, State).
gdl_sentence_star_right_parens2([], _State) --> gdl_right_parens, !.
gdl_sentence_star_right_parens2([Sentence|Sentences], State) --> gdl_sentence(Sentence, State), !, gdl_sentence_star_right_parens(Sentences, State).

% equation ::= (= term term)
% inequality ::= (/= term term)

% relsent ::= (constant term* [seqvar])
% relsent ::= (holds term term* [seqvar])
% only the first without seqvar is gdl
gdl_relsent(Relation, State) --> gdl_left_parens, gdl_white_star, gdl_relsent2_right_parens(Relation, State).
gdl_relsent2_right_parens(Relation, State) --> gdl_constant(Atom), gdl_term_star_right_parens(Terms),
	{	(Atom=distinct ->
			Relation=..['\\='|Terms]
		;
			Relation1=..[Atom|Terms],
			rename_predicate(Relation1, State, Relation)
		)
	}.

% logsent ::= (not sentence) | (and sentence*) | (or sentence*) | (=> sentence* sentence) | (<= sentence sentence*) | (<=> sentence sentence)
% only "not", "and" and "or" can occur in the body of a valid gdl rule
gdl_logsent(Sentence, State) --> gdl_left_parens, gdl_white_star, gdl_logsent2_right_parens(Sentence, State).
gdl_logsent2_right_parens(\+(Sentence), State) --> gdl_not(_), !, gdl_white_star, gdl_sentence(Sentence, State), gdl_white_star, gdl_right_parens.
gdl_logsent2_right_parens(And, State) --> gdl_and(_), !,  gdl_sentence_star_right_parens(Sentences, State), {list_to_and(Sentences, And)}.
gdl_logsent2_right_parens(Or, State) --> gdl_or(_), !,  gdl_sentence_star_right_parens(Sentences, State), {list_to_or(Sentences, Or)}.

gdl_rule(Rule) --> gdl_left_parens, !, gdl_white_star, gdl_rule_right_parens(Rule, _State).
gdl_rule(Rule) --> gdl_relation_constant(Atom, _State),
	{
		Rule=(Atom:-true)
	}.

gdl_rule_right_parens(Rule, State) --> gdl_inv_impl(_), !,
	gdl_white_star,
	gdl_rule_head(Head, State),
	gdl_sentence_star_right_parens(Sentences, State),
	{(Sentences=[] ->  Rule=(Head:-true) ; list_to_and(Sentences, And), Rule=(Head:-And))}.
gdl_rule_right_parens(Rule, State) --> gdl_relsent2_right_parens(Relation, State), {Rule=(Relation:-true)}.

gdl_rule_head(Head, State) --> gdl_relsent(Head, State), !.
gdl_rule_head(Head, State) --> gdl_relation_constant(Head, State).

gdl_relation_constant(Relation, State) -->
	gdl_constant(Atom),
	{
		rename_predicate(Atom, State, Relation)
	}.

% transform lists to prolog conjunctions or disjunctions
list_to_and([],true).
list_to_and([X],X) :- !.
list_to_and([X|List],(X,And)) :- list_to_and(List,And).
list_to_or([],fail).
list_to_or([X],X) :- !.
list_to_or([X|List],(X;Or)) :- list_to_or(List,Or).

gdl_integer_string_list_nonempty([C|List]) --> gdl_digit(C), !, gdl_integer_string_list(List).
gdl_integer_string_list([C|List]) --> gdl_digit(C), !, gdl_integer_string_list(List).
gdl_integer_string_list([]) --> [].
gdl_integer_string(S) --> gdl_integer_string_list_nonempty(CList), {string_list(S,CList)}.
gdl_integer(I) --> gdl_integer_string(S), {number_string(I,S)}.

% quantsent ::= (forall (varspec+) sentence) | (exists (varspec+) sentence)
% varspec ::= variable | (variable constant)

% form ::= sentence | definition
% there are no definitions in gdl and only some types sentences are allowed
gdl_form(Axiom) --> gdl_rule(Axiom)
					%, {writeln(parsed_axiom(Axiom))}
					.

gdl_form_star(Axioms) --> gdl_white_star, gdl_form_star2(Axioms).
gdl_form_star2([Axiom|Axioms]) --> gdl_form(Axiom), !, gdl_form_star(Axioms).
gdl_form_star2([]) --> [].

gdl_knowledge_base([Axiom|Axioms]) --> gdl_white_star, gdl_form(Axiom), gdl_form_star(Axioms).

gdl_message(Message) -->
	gdl_white_star,
	gdl_left_parens,
	gdl_white_star,
	gdl_message2(Message),
	gdl_white_star,
	gdl_right_parens,
	gdl_white_star.

gdl_message2(start(MatchID, Role, Axioms, StartClock, PlayClock)) -->
	[115,116,97,114,116], !, gdl_white_plus, % ="start"
	gdl_constant(MatchID), gdl_white_star,
	gdl_constant(Role), gdl_white_star,
	gdl_left_parens, gdl_knowledge_base(Axioms), gdl_right_parens, gdl_white_star,
	gdl_integer(StartClock), gdl_white_star,
	gdl_integer(PlayClock).
gdl_message2(play(MatchID, Actions)) -->
	[112,108,97,121], !, gdl_white_plus, % ="play"
	gdl_constant(MatchID), gdl_white_star,
	gdl_term_list(Actions).
gdl_message2(replay(MatchID, Actions)) -->
	[114,101,112,108,97,121], !, gdl_white_plus, % ="replay"
	gdl_constant(MatchID), gdl_white_star,
	gdl_term_list(Actions).
gdl_message2(stop(MatchID, Actions)) -->
	[115,116,111,112], !, gdl_white_plus, % ="stop"
	gdl_constant(MatchID), gdl_white_star,
	gdl_term_list(Actions).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% translate some prolog term to gdl/kif
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:- mode translate_to_sexpr_string(+,-).

% this first clause will probably not be used and is just here in case there is something wrong
translate_to_sexpr_string(V,S) :- var(V), !, term_string(V,S1), concat_string(["?",S1],S).

translate_to_sexpr_string([],"nil") :- !.
translate_to_sexpr_string([E|L],String) :- !,
	translate_to_sexpr_string_list([E|L],SAs),
	join_string(SAs," ",SA),
	concat_string(["(",SA,")"],String).

translate_to_sexpr_string(T,String) :-
	T=..[F,A1|As],!, % not an atom
	translate_to_sexpr_string_list([F,A1|As],SAs),
	join_string(SAs," ",SA),
	concat_string(["(",SA,")"],String).
translate_to_sexpr_string(T,S) :- atom(T), !,
	atom_string(T,S).
translate_to_sexpr_string(T,S) :- number(T), !,
	number_string(T,S).
translate_to_sexpr_string(T,S) :- string(T), !,
	S=T.
translate_to_sexpr_string(T,S) :- !,
	term_string(T,S). % this is unsafe and shouldn't be used (but it shouldn't be called anyway)

translate_to_sexpr_string_list([],[]) :- !.
translate_to_sexpr_string_list([A|As],[SA|SAs]) :- !,
	translate_to_sexpr_string(A,SA),
	translate_to_sexpr_string_list(As,SAs).
