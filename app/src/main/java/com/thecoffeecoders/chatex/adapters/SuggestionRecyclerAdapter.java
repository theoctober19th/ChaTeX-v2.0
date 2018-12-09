package com.thecoffeecoders.chatex.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.models.Equation;

import io.github.kexanie.library.MathView;

public class SuggestionRecyclerAdapter extends FirebaseRecyclerAdapter<Equation, SuggestionRecyclerAdapter.SuggestionViewHolder> {

    public SuggestionRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Equation> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position, @NonNull Equation model) {
        String equation = model.getEquation();
        holder.bind(equation);
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_single_equation, viewGroup, false);

        return new SuggestionViewHolder(view);
    }

    public class SuggestionViewHolder extends RecyclerView.ViewHolder{

        MathView mathView;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);

            mathView = itemView.findViewById(R.id.suggestion_mathview);
        }

        public void bind(String equation){
            mathView.setText("\\(" + equation + "\\)");
        }
    }
}
