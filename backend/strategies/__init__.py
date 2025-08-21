"""
Backtesting Strategies Package
""" 

import os
import importlib
from typing import Dict, Type
from backtesting import Strategy

# Dictionary to hold all strategy classes
_strategies: Dict[str, Type[Strategy]] = {}

def get_strategy_class(name: str) -> Type[Strategy]:
    """
    Dynamically retrieves a strategy class by its name.
    """

    # Ensure all strategies are loaded
    if not _strategies:
        _load_strategies()

    return _strategies.get(name)

def get_all_strategies() -> Dict[str, Type[Strategy]]:
    """
    Returns a dictionary of all available strategies.
    """
    if not _strategies:
        _load_strategies()
    return _strategies

def _load_strategies():
    """
    Dynamically loads all strategy classes from the 'strategies' directory.
    """
    
    strategy_files = [f for f in os.listdir(os.path.dirname(__file__)) if f.endswith('.py') and not f.startswith('__')]
    
    for file in strategy_files:
        module_name = f"strategies.{file[:-3]}"
        try:
            module = importlib.import_module(module_name)
            for attr_name in dir(module):
                attr = getattr(module, attr_name)
                if isinstance(attr, type) and issubclass(attr, Strategy) and attr is not Strategy:
                    _strategies[attr_name] = attr
        except ImportError as e:
            print(f"Could not import strategy from {file}: {e}")