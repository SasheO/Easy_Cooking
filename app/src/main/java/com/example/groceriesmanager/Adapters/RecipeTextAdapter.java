package com.example.groceriesmanager.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceriesmanager.Activities.EditRecipeActivity;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.R;

import java.util.List;
import java.util.Objects;

public class RecipeTextAdapter extends RecyclerView.Adapter<RecipeTextAdapter.ViewHolder> {

    private List<FoodItem> ingredientList;
    private List<String> procedureList;
    private EditRecipeActivity context;
    private String type;
    private static final String TAG = "RecipeTextAdapter";

    // Pass in the list array into the constructor
    public RecipeTextAdapter(String type) {
        this.type = type;
    }

    public void IngredientAdapter(Context context, List<FoodItem> items) {
        this.ingredientList = items;
        this.context = (EditRecipeActivity) context;
    }

    public void ProcedureAdapter(Context context, List<String> items) {
        this.procedureList = items;
        this.context = (EditRecipeActivity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.item_ingredient, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (Objects.equals(type, "ingredient")){
            FoodItem ingredient = ingredientList.get(position);
            holder.bind(ingredient);
        }
        else {
            String procedure = procedureList.get(position);
            holder.bind(procedure);
        }
    }

    @Override
    public int getItemCount() {
        if (Objects.equals(type, "ingredient")){
            return ingredientList.size();
        }
        else {
            return procedureList.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        private TextView tvText;
        private ImageButton ibEdit;
        private ImageButton ibDelete;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            itemView.setOnClickListener(this);
            tvText = (TextView) itemView.findViewById(R.id.tvText);
            ibDelete = (ImageButton) itemView.findViewById(R.id.ibDelete);
            ibEdit = (ImageButton) itemView.findViewById(R.id.ibEdit);
        }

        @Override
        public void onClick(View v) {
        }

        public void bind(FoodItem item) {
            String name = item.getName();
            if (item.getQuantity()!=null){
                name = item.getQuantity() + " " + item.getMeasure() + " " + name;
            }
            tvText.setText(name);

            ibEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.editIngredient(item, ingredientList.indexOf(item));
                    Log.i(TAG, ingredientList.toString());
                }
            });

            ibDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.deleteIngredient(item);
                }
            });
        }

        public void bind(String procedure){
            tvText.setText(procedure);

            ibDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.deleteProcedure(procedureList.indexOf(procedure));
                    Log.i(TAG, procedureList.toString());
                }
            });

            ibEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.editProcedure(procedureList.indexOf(procedure));
                }
            });
        }
    }


}
